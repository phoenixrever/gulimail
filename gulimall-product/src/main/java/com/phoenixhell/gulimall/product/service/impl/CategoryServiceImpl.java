package com.phoenixhell.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.CategoryDao;
import com.phoenixhell.gulimall.product.entity.CategoryEntity;
import com.phoenixhell.gulimall.product.service.CategoryBrandRelationService;
import com.phoenixhell.gulimall.product.service.CategoryService;
import com.phoenixhell.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> list = this.list();
        //找到所有分类及其子分类 最后一步list为空  menu自然也为null  所以空指针异常
        List<CategoryEntity> levelOneMenu = list.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, list));
            return menu;
        }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())).collect(Collectors.toList());
        return levelOneMenu;
    }

    //递归查找所有子分类  最后一步list为空  menu自然也为null  所欲空指针异常
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> list) {
        List<CategoryEntity> children = list.stream().filter(menu -> menu.getParentCid().equals(root.getCatId()))
                .map(menu -> {
                    menu.setChildren(getChildren(menu, list));
                    return menu;
                }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())).collect(Collectors.toList());

        return children;
    }


    /**
     * 查找分类路径
     *
     * @param catalogId
     * @return
     */
    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> path = new ArrayList<>();
        List<Long> catalogPath = findParent(catalogId, path);
        // catalogPath.toArray();返回的obj 不能强转
        Collections.reverse(catalogPath);
        return catalogPath.toArray(new Long[catalogPath.size()]);
    }

    private List<Long> findParent(Long catalogId, List<Long> path) {
        path.add(catalogId);
        CategoryEntity category = this.getById(catalogId);
        if (category.getParentCid() != 0) {
            findParent(category.getParentCid(), path);
        }
        return path;
    }

    /**
     * 如果更新字段含有关系中有name name是 category_brand_relation catalog_name
     *
     * @CacheEvict 清除缓存  不需要加category:: 前缀 key写的什么这边就写什么
     * 2种方法 删除多个  这里是失效模式
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevelOneCategories'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'"),
//    })
    //直接失效整个cache name
//    @CachePut 双写模式 更改好的同时写入数据库
    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        boolean updateById = this.updateById(category);
        boolean updateCategory = true;
        if (!StringUtils.isEmpty(category.getName())) {
            updateCategory = categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
        if (!updateById || !updateCategory) {
            throw new RuntimeException(" 更新category关系表 级联错误");
        }
    }

    /**
     * 查出一级分类首页用
     * 每一个缓存都要指定一个名字 相当于缓存的分区（按照业务类型分）
     *
     * @Cacheable({"category"}) 代表当前方法的结果需要缓存，如果方法中有数据当前方法不会执行
     * 如歌缓存中没有会调用方法将结果放入缓存
     * 缓存过的序列是通过jdk序列化的 不需要在转成json存入  或者json转成对象取出
     * 默认行为：
     */
    //key 指定字符串要这样写,key ="'levelOneCategories'"
    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public List<CategoryEntity> getLevelOneCategories() {
        System.out.println("Cacheable");
        List<CategoryEntity> levelOneCategories = this.query().eq("parent_cid", 0).list();
        return levelOneCategories;
    }

    /**
     * 获取首页三级分类
     *  spring cache 设置syn为true(默认false)会调用 synchronized的get读方法 加了本地锁 可以防止缓存击穿
     *  只有 @Cacheable有
     */
    @Cacheable(value = "category", key = "#root.methodName",sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("线程" + Thread.currentThread().getName() + "查询了数据库");
        List<CategoryEntity> categoryEntities = this.listWithTree();
        Map<String, List<Catalog2Vo>> collect = categoryEntities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catalog2Vo> catalog2VoList = v.getChildren().stream().map(item -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                catalog2Vo.setCatalog1Id(v.getCatId().toString());
                catalog2Vo.setId(item.getCatId().toString());
                catalog2Vo.setName(item.getName());
                List<Catalog2Vo.Catalog3Vo> catalog3Vos = item.getChildren().stream().map(item2 -> {
                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                    catalog3Vo.setCatalog2Id(item2.getCatId().toString());
                    catalog3Vo.setId(item2.getCatId().toString());
                    catalog3Vo.setName(item2.getName());
                    return catalog3Vo;
                }).collect(Collectors.toList());
                catalog2Vo.setCatalog3List(catalog3Vos);
                return catalog2Vo;
            }).collect(Collectors.toList());
            return catalog2VoList;
        }));
        return collect;
    }


    /**
     * 分布式锁
     * 构造返回前端的json数据
     * 缓存里面的数据如何和数据库保持一致
     * 这就是缓存一致性问题
     * 1) 双写模式
     * 2) 失效模式
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonWithRedisson() {
        //通过 redisson 占分布式锁
        //锁的名字 代表的锁越多 锁的粒度越大 运行越慢
        //约定命名 11号商品  product-11-lock
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> collect = null;
        try {
            collect = getCatalogJsonFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return collect;
    }


    //====================没有缓存和redisson之前的方法=========================

    /**
     * 构造返回前端的json数据
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonWithLocalLock() {
        //加锁  只要是同一把锁，就能锁住需要这个锁的所有线程
        //1 synchronized (this) springboot 所有的组件在容器中都是单例的 this可以
        //也可以直接用类  CategoryServiceImpl.class
        //TODO 本地锁 synchronized juc(lock) 只能锁当前进程 也就是当前微服务 要加分布式锁
        synchronized (this) {
            Map<String, List<Catalog2Vo>> collect = getCatalogJsonFromDB();
            return collect;
        }
    }

    /**
     * 利用redis set NX 产生分布式锁
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonWithRedisLock() {
        //尝试去redis set NX lock
        //absent 缺席; 不在  =set NX
        //设置过期时间 和加锁要同步的原子性 防止意外不能释放锁
//        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "lock");
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功");
            Map<String, List<Catalog2Vo>> collect = null;
            try {
                //加锁成功
                collect = getCatalogJsonFromDB();
            } finally {
                //释放锁 获取值和自己uuid对比+删除锁 必须是原子操作 lua 脚本解锁
                //Keys[1] 为我们的key lock ARGV[1] 我们传入的uuid value 值
                String scripts = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                //private @Nullable Class<T> resultType;
                // 泛型可以不写 构造函数一定要写 execute(RedisScript<T> script, List<K> keys, Object... args)  uuid不是list
                //这里只能是Long
                Long lockValue = stringRedisTemplate.execute(new DefaultRedisScript<>(scripts, Long.class), Arrays.asList("lock"), uuid);
                System.out.println(lockValue + "------------------");
            }

//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if(uuid.equalsIgnoreCase(lockValue)){
//                //删除自己的锁
//                stringRedisTemplate.delete("lock");
//            }
            return collect;
        } else {
            System.out.println("获取分布式锁失败");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonWithRedisLock();
        }

    }

    /**
     * 没有用redis缓存前的查数据库
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        //真正查询之前看看有其他抢到锁的有没写缓存
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2Vo>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return map;
        }
        System.out.println("线程" + Thread.currentThread().getName() + "查询了数据库");
        List<CategoryEntity> categoryEntities = this.listWithTree();
        Map<String, List<Catalog2Vo>> collect = categoryEntities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catalog2Vo> catalog2VoList = v.getChildren().stream().map(item -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                catalog2Vo.setCatalog1Id(v.getCatId().toString());
                catalog2Vo.setId(item.getCatId().toString());
                catalog2Vo.setName(item.getName());
                List<Catalog2Vo.Catalog3Vo> catalog3Vos = item.getChildren().stream().map(item2 -> {
                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                    catalog3Vo.setCatalog2Id(item2.getCatId().toString());
                    catalog3Vo.setId(item2.getCatId().toString());
                    catalog3Vo.setName(item2.getName());
                    return catalog3Vo;
                }).collect(Collectors.toList());
                catalog2Vo.setCatalog3List(catalog3Vos);
                return catalog2Vo;
            }).collect(Collectors.toList());
            return catalog2VoList;
        }));
        //redisson解决一切
//        if (collect == null) {
//            //穿透 缓存null结果并加入短暂过期时间(此处仅示范，collect不可能等于null)
//            stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(0), 1, TimeUnit.DAYS);
//        } else {
//            //击穿：原有的失效时间上加入一个随机值增量
//            stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(collect), Duration.ofSeconds(new Random().nextInt(60 * 60 * 24)));
//        }
        return collect;
    }
}