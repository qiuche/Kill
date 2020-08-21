package com.kill.model.mapper;

import com.kill.model.entity.ItemKill;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.CacheConfig;

import java.util.List;


public interface ItemKillMapper {
    List<ItemKill> selectAll();

    List<String> selectAllKillId();

    ItemKill selectById(@Param("id")Integer id);

    int updateKillItem(@Param("killId") Integer killId);

    int addKillItemTotal(@Param("killId") Integer killId);
}
