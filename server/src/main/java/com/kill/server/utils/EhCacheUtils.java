package com.kill.server.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import javax.annotation.Resource;


@Component
public class EhCacheUtils {

	// @Autowired
	// private CacheManager ehCacheCacheManager;
	@Autowired(required=false)
	private EhCacheCacheManager ehCacheCacheManager;

	//处理缓存分组名，不存在就创建
	public Cache getOrAddCache(String cacheName) {
		Cache cache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
		if (cache == null) {
			synchronized (this) {
				cache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
				if (cache == null) {
					ehCacheCacheManager.getCacheManager().addCacheIfAbsent(cacheName);
					cache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
				}
			}
		}
		return cache;
	}

	// cacheName 和 key 区别 就是 redis中的db库 组
	// 添加本地缓存 (相同的key 会直接覆盖)
	public void put(String cacheName, String key, Object value) {
		Cache cache = getOrAddCache(cacheName);
		Element element = new Element(key, value);
		cache.put(element);
	}

	// 获取本地缓存
	public Object get(String cacheName, String key) {
		Cache cache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
		Element element = cache.get(key);
		return element == null ? null : element.getObjectValue();
	}

	//清除缓存
	public void remove(String cacheName, String key) {
		Cache cache = ehCacheCacheManager.getCacheManager().getCache(cacheName);
		cache.remove(key);
	}

}
