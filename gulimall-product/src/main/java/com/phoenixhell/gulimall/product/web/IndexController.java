package com.phoenixhell.gulimall.product.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.phoenixhell.gulimall.product.entity.CategoryEntity;
import com.phoenixhell.gulimall.product.service.CategoryService;
import com.phoenixhell.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    @GetMapping({"/", "/index", "index.html"})
    public String index(Model model) {
        List<CategoryEntity> levelOneCategories = categoryService.getLevelOneCategories();
        model.addAttribute("categories", levelOneCategories);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }


    //========================没有引进缓存的做法======================================
    //1 springboot2.0 以后默认使用lettuce 错误操作redis的客户端他使用netty进行网络通信
    //2 lettuce bug导致
    //如果设置了jvm 比如-Xmx300m ，netty如果没有指定内存会使用我们设置的jvm内存导致堆外溢出
    //可以通过   -Dio.netty.maxDirectMemory 进行设置
    //lettuce jeais 都是操作redis的底层客户端 spring再次封装成redisTemplate
    // 1升级lettuce 2更换jedis
    @ResponseBody
    @GetMapping("/index/catalog.json2")
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        /*
         *总结:(都是在同一时刻大并发前提下)
         * 	• 穿透：查询一个永不存在的数据，大并发都跑到数据库
         * 		○ 解决：缓存null结果并加入短暂过期时间
         * 	• 击穿：有一个key特别火，redis失效了，这个key的所有请求都同一时刻来查数据库
         * 		○ 解决：原有的失效时间上加入一个随机值增量
         * 	• 雪崩：大面积的缓存同意时刻失效，都要查询数据库
         *      ○ 解决：加锁，大并发只让一个人查好放缓存，其他人等待缓存数据
         */
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        Map<String, List<Catalog2Vo>> map;
        if (!StringUtils.isEmpty(catalogJson)) {
            System.out.println("缓存命中");
            map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        } else {
            System.out.println("缓存不命中");
            map = categoryService.getCatalogJson();
        }
        return map;
    }

    /**
     * 简单服务测试nginx+gateway组合的性能
     * ReentrantLock 可重入锁
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        RLock anyLock = redissonClient.getLock("anyLock");
//       new Thread(()->doWork(anyLock),"AAA").start();
//       new Thread(()->doWork(anyLock),"BBB").start();
        doWork(anyLock);
        return "hello";
    }

    void doWork(RLock lock) {
        String name = Thread.currentThread().getName();
        System.out.println(name + " 开始尝试获取锁");
        /**
         * 1 . 锁的自动续期 如果业务超长 运行期间会自动续期到30秒 不用担心业务时间长锁自动过期被删掉
         * 2  加锁的业务只要运行完成 就不会给当前锁续期 即使不手动解锁 锁默认在30秒后自行删除
         * 3 如果指定时间 到期不会续期
         * 如果我们传递了锁的超时时间 发送lua 脚本给redis 占锁
         * 如果没有指定时间就使用  lockWatchdogTimeout = 30 * 1000; 看门狗默认时间
         * 如果没有指定时间  只要占锁成功就会启动一个定时任务 重新给锁设置过期时间  默认是看门狗的默认事件30S
         *占锁成功后 每过  internalLockLeaseTime / 3 的时间自动续期
         */
        try {
            lock.lock(30, TimeUnit.SECONDS); //阻塞式等待 默认加锁时间为30秒
            System.out.println(name + " 得到锁");
            System.out.println(name + " 开工干活");
            for (int i = 0; i < 100; i++) {
                Thread.sleep(1000);
                System.out.println(name + " : " + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println(name + " 释放锁");
                lock.unlock();
            } catch (Exception e) {
                //防止释放锁的时候锁已经被删了
                System.out.println(name + " : 没有得到锁的线程运行结束");
            }

        }
    }

    //==============================读写锁====================================

    /**
     * ReadWriteLock
     * 读写锁
     * 写时加锁独占   读时并发
     * 只要写锁存在 其他写锁读锁都必须等待
     * 加读锁时候 写锁也需要等待  读不需要
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        String writeValue = null;
        RReadWriteLock readWriteLock = null;
        try {
            readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
            readWriteLock.writeLock().lock();
            System.out.println(Thread.currentThread().getName() + " ------writing");
            writeValue = UUID.randomUUID().toString();
            TimeUnit.SECONDS.sleep(15);
            stringRedisTemplate.opsForValue().set("writeValue", writeValue);
            System.out.println(Thread.currentThread().getName() + " -----write complete");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return writeValue;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        String value = null;
        RReadWriteLock readWriteLock = null;
        try {
            readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
            readWriteLock.readLock().lock();
            System.out.println(Thread.currentThread().getName() + "------reading");
            value = stringRedisTemplate.opsForValue().get("writeValue");
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(Thread.currentThread().getName() + "\t +" + value + "-----reading complete");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        return value;
    }
    //=================信号量  Semaphore =============================

    /**
     * 信号量可以限流
     */
    @ResponseBody
    @GetMapping("/semaphore")
    public String semaphoreTest() {
        RSemaphore semaphoreLock = null;
        try {
            /*
                每acquire 一次 semaphoreLock 减1
                TODO 同一个浏览器项目卡 不断刷新同并发 不同的只能前后执行 不指定什么原因
             */
            semaphoreLock = redissonClient.getSemaphore("semaphoreLock");
            System.out.println(semaphoreLock.availablePermits());
            semaphoreLock.acquire();
            System.out.println(Thread.currentThread().getName() + "\t take the position");
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphoreLock.release();
            System.out.println(Thread.currentThread().getName() + "\t after 3s leave the position");
        }
        return "semaphore";
    }

    //=================闭锁 CountDownLatch  =============================
    @ResponseBody
    @GetMapping("/countdown")
    public String countdownTest() {
        RCountDownLatch countDownLatchLock = null;
        try {
            countDownLatchLock = redissonClient.getCountDownLatch("countDownLatchLock");
            countDownLatchLock.countDown();
            System.out.println("灭一国");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  Thread.currentThread().getName()+" 灭一国 countDownLatchLock -1";
    }

    @ResponseBody
    @GetMapping("/wait")
    public String waitTest() {
        RCountDownLatch countDownLatchLock = null;
        try {
            countDownLatchLock = redissonClient.getCountDownLatch("countDownLatchLock");
            //这里可以初始化countDownLatchLock的值
            countDownLatchLock.trySetCount(6);
            System.out.println("进军六国开始");
            //阻塞等待countDownLatchLock值为0
            countDownLatchLock.await();
            System.out.println("六国全灭 删除countDownLatchLock");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "六国全灭 删除 countDownLatchLock";
    }
}
