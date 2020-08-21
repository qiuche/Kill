package com.kill.server.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.kill.server.Filter.BloomFilterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;


@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

        private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

        @Autowired
        private RedisConnectionFactory redisConnectionFactory;

        @Bean
        public KeyGenerator keyGenerator(){
            return (o, method, params) ->{
                StringBuilder sb = new StringBuilder();
                sb.append(o.getClass().getName()); // 类目
                sb.append(method.getName()); // 方法名
                for(Object param: params){
                    sb.append(param.toString()); // 参数名
                }
                return sb.toString();
            };
        }
//        @Bean
//        public RedisTemplate<String, Object> redisTemplate() {
//            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//            redisTemplate.setConnectionFactory(redisConnectionFactory);
//            redisTemplate.setKeySerializer(new StringRedisSerializer());
//            redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
//            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//            return redisTemplate;
//        }
//
//        @Bean
//        public StringRedisTemplate stringRedisTemplate() {
//            StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
//            stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
//            return stringRedisTemplate;
//        }
//
//        @Bean
//        public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
//            StringRedisTemplate template = new StringRedisTemplate(factory);
//            //定义value的序列化方式
//            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//            ObjectMapper om = new ObjectMapper();
//            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//            om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//            jackson2JsonRedisSerializer.setObjectMapper(om);
//
//            template.setValueSerializer(jackson2JsonRedisSerializer);
//            template.setHashValueSerializer(jackson2JsonRedisSerializer);
//            template.afterPropertiesSet();
//            return template;
//        }

        @Bean
        @Primary
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(60000000)) // 60s缓存失效
                    // 设置key的序列化方式
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                    // 设置value的序列化方式
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()))
                    // 不缓存null值
                    .disableCachingNullValues();

            RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(config)
                    .transactionAware()
                    .build();

            logger.info("自定义RedisCacheManager加载完成");
            return redisCacheManager;
        }

        // key键序列化方式
        private RedisSerializer<String> keySerializer() {
            return new StringRedisSerializer();
        }

        // value值序列化方式
        private GenericJackson2JsonRedisSerializer valueSerializer(){
            return new GenericJackson2JsonRedisSerializer();
        }

        //初始化布隆过滤器，放入到spring容器里面
        @Bean
        public BloomFilterHelper<String> initBloomFilterHelper() {
            return new BloomFilterHelper<>((Funnel<String>) (from, into) -> into.putString(from, Charsets.UTF_8).putString(from, Charsets.UTF_8), 1000000, 0.01);
        }

}

