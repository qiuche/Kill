package com.kill.server.service;

import com.kill.model.entity.ItemKillSuccess;
import com.kill.model.mapper.ItemKillMapper;
import com.kill.model.mapper.ItemKillSuccessMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SchedulerService {
    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private Environment env;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void schedulerExpireOrders() {
        try {
            log.info("执行定时任务，防止死信队列宕机，订单超时未处理");
            List<ItemKillSuccess> itemKillSuccesses = itemKillSuccessMapper.selectExpireOrders();
            if (itemKillSuccesses != null && !itemKillSuccesses.isEmpty()) {
                itemKillSuccesses.stream().forEach(i -> {
                    if (i != null && i.getDiffTime() > env.getProperty("scheduler.expire.orders.time", Integer.class) && i.getStatus() == 0) {
                        itemKillSuccessMapper.expireOrder(i.getCode());
                        itemKillMapper.addKillItemTotal(i.getKillId());
                        stringRedisTemplate.delete(env.getProperty("redis.item.kill.Killedkey")+i.getKillId()+i.getUserId());
                    }
                });
            }
        } catch (Exception e) {
            log.error("定时获取status=0的订单并判断是否超过TTL，然后进行失效-发生异常：", e.fillInStackTrace());
        }
    }
}
