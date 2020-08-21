package com.kill.server.controller;

import ch.qos.logback.core.status.StatusUtil;
import com.kill.api.enums.StatusCode;
import com.kill.api.response.BaseResponse;
import com.kill.model.entity.ItemKill;
import com.kill.server.Filter.BloomFilterHelper;
import com.kill.server.Filter.RedisBloomFilter;
import com.kill.server.service.IItemService;
import com.kill.server.utils.EhCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
public class CacheController {
    @Autowired
    private EhCacheUtils ehCacheUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private IItemService itemService;

    @Autowired
    private Environment env;

    @Autowired
    RedisBloomFilter redisBloomFilter;

    @Autowired
    private BloomFilterHelper bloomFilterHelper;

    private static final String cacheName="ItemCache";

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    @RequestMapping(value = "/RedisCache/remove")
    public BaseResponse removeRedisCache(String key){
        BaseResponse response=new BaseResponse();
        try {
            if(key.equals("*"))
            {
                itemService.RemoveCacheKillItems();
                Set<String> keys=redisTemplate.keys(key+"*");
                redisTemplate.delete(keys);
                response.setCode(StatusCode.Success.getCode());
                response.setMsg("清除商品id和数量信息所有缓存成功");
            }
            else if(key.equals(cacheName))
            {
                response.setCode(StatusCode.Success.getCode());
                response.setMsg("清除商品id和数量信息Cache缓存成功");
                itemService.RemoveCacheKillItems();
            }
            else {
                Set<String> keys=redisTemplate.keys(key+"*");
                redisTemplate.delete(keys);
                response.setCode(StatusCode.Success.getCode());
                response.setMsg("清除商品id和数量信息redis缓存成功");
            }
        }catch (Exception e){
            response.setCode(StatusCode.Fail.getCode());
            response.setMsg("清除商品id和数量信息缓存失败");
            response.setData(e.fillInStackTrace());
        }
        return response;
    }

    @Scheduled(cron = "0 0/10 * * * ?")//十分钟刷新一次缓存
    @RequestMapping(value = "/Cache/update")
    public BaseResponse updateCache() throws Exception {
        BaseResponse response=new BaseResponse();
        try {
            this.removeRedisCache("*");
            List<ItemKill> killItems = itemService.getKillItems();
            for (ItemKill item : killItems) {
                ehCacheUtils.put("ItemCache","LocalCacheKey-"+item.getId(),item.getTotal());
                redisTemplate.opsForValue().set(env.getProperty("redis.item.kill.Itemkey")+item.getId(),item.getTotal()+"",60 * 10+new Random().nextInt(100), TimeUnit.SECONDS);
                redisBloomFilter.addByBloomFilter(bloomFilterHelper,"bloom",item.getId()+"");
            }
            response.setCode(StatusCode.Success.getCode());
            response.setMsg("更新商品id和数量信息缓存成功");
            log.info("更新商品id和数量信息缓存");
        }catch (Exception e){
            response.setCode(StatusCode.Fail.getCode());
            response.setMsg("更新商品id和数量信息缓存失败");
            response.setData(e.fillInStackTrace());
        }
        return response;
    }


}
