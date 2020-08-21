package com.kill.model.mapper;

import com.kill.model.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(User record);

    User selectByUserName(@Param("userName") String userName);

    User selectByUserNamePsd(@Param("userName") String userName,@Param("password") String password);
}
