package com.kill.server.service;

import com.kill.model.entity.ItemKill;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface IItemService {
    List<ItemKill> getKillItems() throws Exception;

    ItemKill getKillDetail(Integer id)throws Exception;
}
