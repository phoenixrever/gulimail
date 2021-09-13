package com.phoenixhell.gulimall.product;

import com.phoenixhell.gulimall.product.entity.BrandEntity;
import com.phoenixhell.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("苹果");
        brandService.save(brandEntity);
        System.out.println(brandService.list());
    }

    @Test
    void testRedis() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        String scripts = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        // 泛型可以不写 构造函数一定要写 execute(RedisScript<T> script, List<K> keys, Object... args)
        stringRedisTemplate.execute(new DefaultRedisScript<>(scripts, Integer.class), Arrays.asList("lock"), uuid);

    }

    @Test
    void testRedisson() {
        System.out.println(redissonClient);
    }

    @Test
    void testSteeam() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        List<Integer> collect = list.stream().filter(x -> {
            return  x % 2 == 0;
        }).collect(Collectors.toList());
        System.out.println(collect);
    }
}
