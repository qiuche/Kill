package com.kill.server.service.impl;

import com.kill.api.enums.SysConstant;
import com.kill.model.entity.ItemKill;
import com.kill.model.entity.ItemKillSuccess;
import com.kill.model.mapper.ItemKillMapper;
import com.kill.model.mapper.ItemKillSuccessMapper;
import com.kill.server.service.IKillService;
import com.kill.server.service.RabbitSenderService;
import com.kill.server.utils.SnowFlake;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KillService implements IKillService {
    private static final Logger log= LoggerFactory.getLogger(KillService.class);

    private SnowFlake snowFlake=new SnowFlake(2,3);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitSenderService rabbitSenderService;

    @Override
    public Boolean KillItem(Integer killId, Integer userId) throws Exception {
        Boolean result=false;
        if(itemKillSuccessMapper.countByKillUserId(killId,userId)<=0){
            ItemKill itemKill = itemKillMapper.selectById(killId);
            if(itemKill!=null&&1==itemKill.getCanKill()&&itemKill.getTotal()>0){

                int i = itemKillMapper.updateKillItem(killId);
                if(i>0){
                    commonRecordKillSuccessInfo(itemKill,userId);
                    result=true;
                }
            }
        }
        else {
            throw new Exception("您已经抢购过该商品！");
        }
        return result;
    }

    private void commonRecordKillSuccessInfo(ItemKill kill,Integer userId) throws  Exception{
        ItemKillSuccess itemKillSuccess = new ItemKillSuccess();
        String orderNo=String.valueOf(snowFlake.nextId());

        itemKillSuccess.setCode(orderNo);
        itemKillSuccess.setItemId(kill.getItemId());
        itemKillSuccess.setKillId(kill.getId());
        itemKillSuccess.setUserId(userId.toString());
        itemKillSuccess.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        itemKillSuccess.setCreateTime(DateTime.now().toDate());

        if(itemKillSuccessMapper.countByKillUserId(kill.getId(),userId)<=0){
            int i = itemKillSuccessMapper.insertSelective(itemKillSuccess);
            log.info("进入");
            if(i>0){
                log.info("发邮件");
                rabbitSenderService.sendKillSuccessEmailMsg(orderNo);
            }
        }

    }
}
