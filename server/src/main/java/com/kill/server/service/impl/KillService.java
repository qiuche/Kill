package com.kill.server.service.impl;

import com.google.common.collect.Maps;
import com.kill.api.enums.SysConstant;
import com.kill.model.dto.KillDto;
import com.kill.model.dto.KillSuccessUserInfo;
import com.kill.model.entity.ItemKill;
import com.kill.model.entity.ItemKillSuccess;
import com.kill.model.mapper.ItemKillMapper;
import com.kill.model.mapper.ItemKillSuccessMapper;
import com.kill.server.service.IKillService;
import com.kill.server.service.RabbitSenderService;
import com.kill.server.utils.SnowFlake;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class KillService implements IKillService {
    private static final Logger log = LoggerFactory.getLogger(KillService.class);

    private SnowFlake snowFlake = new SnowFlake(2, 3);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitSenderService rabbitSenderService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CuratorFramework curatorFramework;

    private static final String pathPrefix = "/kill/zkLock/";



    /**
     * 商品秒杀核心业务逻辑的处理-基于ZooKeeper的分布式锁
     *
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean KillItem(Integer killId, Integer userId) throws Exception {
        Boolean result = false;
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, pathPrefix + killId + userId + "-locks");
        try {
            if (mutex.acquire(10L, TimeUnit.SECONDS)) {
                //TODO:核心业务逻辑
                if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
                    ItemKill itemKill = itemKillMapper.selectById(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                        int res = itemKillMapper.updateKillItem(killId);
                        if (res > 0) {
                            commonRecordKillSuccessInfo(itemKill, userId);
                            result = true;
                        }
                    } else {
                        log.error("商品不符合抢购要求");
                        throw new Exception("商品不符合抢购要求!");
                    }
                } else {
                    log.error("zookeeper-您已经抢购过该商品了!");
                    throw new Exception("zookeeper-您已经抢购过该商品了!");
                }
            }
        } finally {
            if (mutex != null) {
                mutex.release();
            }
        }
        return result;
    }


    private void commonRecordKillSuccessInfo(ItemKill kill, Integer userId) throws Exception {
        ItemKillSuccess itemKillSuccess = new ItemKillSuccess();
        String orderNo = String.valueOf(snowFlake.nextId());
        itemKillSuccess.setCode(orderNo);
        itemKillSuccess.setItemId(kill.getItemId());
        itemKillSuccess.setKillId(kill.getId());
        itemKillSuccess.setUserId(userId.toString());
        itemKillSuccess.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        itemKillSuccess.setCreateTime(DateTime.now().toDate());
        try {
            if (itemKillSuccessMapper.countByKillUserId(kill.getId(), userId) <= 0) {
                int i = itemKillSuccessMapper.insertSelective(itemKillSuccess);
                if (i > 0) {
                    log.info("发邮件");
                    rabbitSenderService.sendKillSuccessEmailMsg(orderNo);

                    rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNo);
                }
            }
        } catch (Exception e) {
            throw new Exception("邮件发送异常");
        }

    }

    @Autowired
    private Environment env;

    @Override
    public Map<String, Object> checkUserKillResult(Integer killId, Integer userId) throws Exception {
        Map<String, Object> dataMap = Maps.newHashMap();
        KillSuccessUserInfo info = itemKillSuccessMapper.selectByKillIdUserId(killId, userId);
        if (info != null) {
            dataMap.put("executeResult", String.format(env.getProperty("notice.kill.item.success.content"), info.getItemName()));
            dataMap.put("info", info);
        } else {
            throw new Exception(env.getProperty("notice.kill.item.fail.content"));
        }
        return dataMap;
    }

}