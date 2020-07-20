package com.kill.server.service.impl;

import com.kill.model.entity.ItemKill;
import com.kill.model.mapper.ItemKillMapper;
import com.kill.server.service.IItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService implements IItemService {

    private static final Logger log= LoggerFactory.getLogger(ItemService.class);

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Override
    @Cacheable(cacheNames = "AllKillItems",key="'AllKillItems'")
    public List<ItemKill> getKillItems() throws Exception {
        return itemKillMapper.selectAll();
    }

    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        System.out.println(id);
        ItemKill entity=itemKillMapper.selectById(id);
        if(entity==null){
            throw  new Exception("获取秒杀详情-待秒杀商品记录不存在");
        }
        return entity;
    }
}
