package com.kill.server.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

/**
 * 本地堆缓存配置类
 * @author asus
 */
@Configuration
@EnableCaching
public class EhcacheConfig {

	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
		EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
		cacheManagerFactoryBean.setShared(true);
		return cacheManagerFactoryBean;
	}

	@Bean
	public EhCacheCacheManager eCacheCacheManager(EhCacheManagerFactoryBean bean) {
		return new EhCacheCacheManager(bean.getObject());
	}
}
