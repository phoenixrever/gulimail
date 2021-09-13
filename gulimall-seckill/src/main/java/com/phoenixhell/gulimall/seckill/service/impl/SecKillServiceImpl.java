package com.phoenixhell.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.seckill.To.SecKillSkuRedisTo;
import com.phoenixhell.gulimall.seckill.feign.CouponFeignService;
import com.phoenixhell.gulimall.seckill.feign.ProductFeignService;
import com.phoenixhell.gulimall.seckill.service.SecKillService;
import com.phoenixhell.gulimall.seckill.vo.SecKillSessionsWithSkusVo;
import com.phoenixhell.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SecKillServiceImpl implements SecKillService {
    public final String SESSION_CACHE_PREFIX = "seckill:session:";
    public final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    public final String SKUSTOCK_SEMAPHORE = "seckill:stock:"; //后面接商品随机码

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public void upSecKillScheduledLast3Days() {
        //扫描最近3天需要参与秒杀活动的商品
        R r = couponFeignService.getLast3DaysSession();
        if (r.getCode() != 0) {
            return;
        }
        List<SecKillSessionsWithSkusVo> sessions = r.getData("sessions", new TypeReference<List<SecKillSessionsWithSkusVo>>() {
        });
        /**
         * redis 里面存储结构
         * 1 缓存活动信息 时间 --  商品skuId
         * 2 缓存活动关联的商品信息 商品skuId  对应的秒杀属性信息
         */
        if (sessions == null || sessions.size() == 0) {
            return;
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            BoundListOperations<String, String> listOperations = redisTemplate.boundListOps(key);
            List<String> range = listOperations.range(0, -1);
            if (session.getSeckillSkuRelationEntities().size() > 0) {
                //活动 商品 skuid
                session.getSeckillSkuRelationEntities().forEach(s -> {
                    if (!operations.hasKey(session.getId() + "_" + s.getSkuId())) {
                        //缓存活动关联的商品信息
                        SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                        //1 秒杀信息
                        BeanUtils.copyProperties(s, secKillSkuRedisTo);
                        //2 sku商品信息
                        R info = productFeignService.getSkuInfo(s.getSkuId());
                        if (info.getCode() == 0) {
                            SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            //未关联的商品活动 不添加到redis
                            if (skuInfo == null) {
                                /*
                                 * 对于java8中的特殊写法lamada表达式中,不能使用break,会提示错误;
                                 * java8中使用return,会跳出当前循环,继续下一次循环,作用类似continue;
                                 * java8中使用foreach,但是不是lamada表达式写法,可以正常使用break或者return,可以直接跳出循环.
                                 */
                                return;
                            }
                            secKillSkuRedisTo.setSkuInfoVo(skuInfo);
                        }
                        secKillSkuRedisTo.setStartTime(startTime);
                        secKillSkuRedisTo.setEndTime(endTime);

                        // 秒杀随机码 防止构造post请求
                        String code = UUID.randomUUID().toString().substring(0, 6);
                        secKillSkuRedisTo.setRandomCode(code);

                        String jsonString = JSON.toJSONString(secKillSkuRedisTo);
                        //保存商品 场次活动id+ 商品skuId
                        operations.put(session.getId() + "_" + s.getSkuId(), jsonString);

                        //商品可以秒杀的库存作为信号量  限流  就是多线程抢资源
                        //等同于 7辆车抢3车位
                        if (!redisTemplate.hasKey(SKUSTOCK_SEMAPHORE + code)) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHORE + code);
                            semaphore.trySetPermits(s.getSeckillCount());
                        }
                    }
                    //left push  现有数据的上面  right 现有数据的下面(尾部)
                    //缓存活动信息    //保存商品 场次活动id+ 商品skuId

                    boolean contains = range.contains(session.getId() + "_" + s.getSkuId());
                    if (!contains) {
                        listOperations.rightPush(session.getId() + "_" + s.getSkuId());
                    }
                });
            }

        });
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSecKillSkus() {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //先去秒杀场次查询符合当前场次的商品skuId   SESSION_CACHE_PREFIX = "seckill:session:";
        List<SecKillSkuRedisTo> secKillSkuRedisTos = new ArrayList<>();
        long time = System.currentTimeMillis();
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                String timeString = key.replace(SESSION_CACHE_PREFIX, "");
                String[] split = timeString.split("_");
                //返回正在秒杀时间内的商品
                if (time >= Long.parseLong(split[0]) && time <= Long.parseLong(split[1])) {
                    BoundListOperations<String, String> listOperations = redisTemplate.boundListOps(key);
                    List<String> range = listOperations.range(0, -1);
                    if (range != null && range.size() > 0) {
                        //range.forEach(item -> {
                        //    String o = operations.get(item);
                        //    if (o != null) {
                        //        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(o.toString(), new TypeReference<SecKillSkuRedisTo>() {
                        //        });
                        //        //当前秒杀已经开始可以带上随机码
                        //        //secKillSkuRedisTo.setRandomCode(null);
                        //        secKillSkuRedisTos.add(secKillSkuRedisTo);
                        //    }
                        //});
                        // multiGet  提示 直接获取多个key的值  前提是声明绑定时候所有数据都为string
                        //BoundHashOperations<String, String, String> operations
                        List<String> multiGet = operations.multiGet(range);
                        if (!StringUtils.isEmpty(multiGet)) {
                            List<SecKillSkuRedisTo> skuRedisTos = JSON.parseObject(multiGet.toString(), new TypeReference<List<SecKillSkuRedisTo>>() {});
                            //当前秒杀已经开始可以带上随机码
                            secKillSkuRedisTos.addAll(skuRedisTos);
                        }
                    }
                    //当前场次符合的只有一个
                    //break;
                }
            }
        }
        return secKillSkuRedisTos;
    }

    @Override
    public SecKillSkuRedisTo getSecKillInfoBySkuId(Long skuId) {
        //找到所有参与秒杀的商品
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = operations.keys();
        if(keys!=null && keys.size()>0){
            for (String key : keys) {
                String[] split = key.split("_");
                boolean b = split[1].equals(skuId.toString());
                if(b){
                    String s = operations.get(key);
                    SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(s, new TypeReference<SecKillSkuRedisTo>() {});
                    long currentTimeMillis = System.currentTimeMillis();
                    //不在秒杀时间不返回随机码
                    if(currentTimeMillis<secKillSkuRedisTo.getStartTime() || currentTimeMillis>secKillSkuRedisTo.getEndTime()){
                        secKillSkuRedisTo.setRandomCode(null);
                    }
                    return secKillSkuRedisTo;
                }
            }
        }
        return null;
    }
}
