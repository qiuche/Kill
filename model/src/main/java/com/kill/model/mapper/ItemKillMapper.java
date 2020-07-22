package com.kill.model.mapper;

import com.kill.model.entity.ItemKill;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemKillMapper {
    List<ItemKill> selectAll();

    ItemKill selectById(@Param("id")Integer id);

    int updateKillItem(@Param("killId") Integer killId);

    int addKillItemTotal(@Param("killId") Integer killId);
}
