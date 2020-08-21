package com.kill.model.mapper;

import com.kill.model.dto.KillDto;
import com.kill.model.dto.KillSuccessUserInfo;
import com.kill.model.entity.ItemKillSuccess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemKillSuccessMapper {
    int deleteByPrimaryKey(String code);

    int insertSelective(ItemKillSuccess record);

    ItemKillSuccess selectByPrimaryKey(String code);

    int updateByPrimaryKey(ItemKillSuccess record);

    int countByKillUserId(@Param("killId") Integer killId,@Param("userId") Integer userId);

    KillSuccessUserInfo selectByCode(@Param("code") String code);

    int expireOrder(@Param("code") String code);

    List<ItemKillSuccess> selectExpireOrders();

    List<KillDto> selectKidUid();

    KillSuccessUserInfo selectByKillIdUserId(@Param("killId") Integer killId, @Param("userId") Integer userId);
}
