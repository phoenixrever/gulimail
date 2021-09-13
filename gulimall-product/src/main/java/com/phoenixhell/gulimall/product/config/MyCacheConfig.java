package com.phoenixhell.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@EnableCaching
@Configuration
public class MyCacheConfig {
    /**
     * 自定义配置文件的话 yaml里面配置的东西就不会调用了
     * 也需要在配置bean里面调用yaml配置属性让其生效
     * 参数里面可以直接注入CacheProperties.class配置类
     * 也可以 用旧版本方法
     *    @ConfigurationProperties(prefix = "spring.cache")
     *    public class CacheProperties {}
     * @EnableConfigurationProperties(CacheProperties.class)
     * @AutoWird 注入配置类
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        //	static RedisSerializer<Object> json() {
        //		return new GenericJackson2JsonRedisSerializer();
        //如果导入了fastJson 也可以用 new GenericFastJsonRedisSerializer()
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return  config;
    }
}
