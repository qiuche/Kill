package com.kill.server.service;

import com.kill.model.entity.ItemKill;

import java.util.List;

public interface IItemService {
    List<ItemKill> getKillItems() throws Exception;

    ItemKill getKillDetail(Integer id)throws Exception;

    List<String> getKillItemsId() throws Exception;

    public void RemoveCacheKillItems() ;
}
