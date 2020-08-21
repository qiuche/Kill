package com.kill.server.service;

import com.kill.model.entity.User;
import com.kill.model.mapper.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;


public class CustomRealm extends AuthorizingRealm {

    private static final Logger log= LoggerFactory.getLogger(CustomRealm.class);

    private static final Long sessionKeyTimeOut=3600_000L;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token= (UsernamePasswordToken) authenticationToken;
        String userName=token.getUsername();
        String password=String.valueOf(token.getPassword());
        log.info("当前登录的用户名={} 密码={}",userName,password);
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            throw new UnknownAccountException("用户名不存在");
        }
        if(!Objects.equals(1,user.getIsActive().intValue())){
            throw new DisabledAccountException("该用户被禁用");
        }
        if(!user.getPassword().equals(password)){
            throw new IncorrectCredentialsException("用户名密码不匹配");
        }
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(user.getUserName(),password,getName());
        setSession("uid",user.getId());
        return info;
    }

    private void setSession(String key,Object value){
        Session session=SecurityUtils.getSubject().getSession();
        if(session!=null){
            session.setAttribute(key,value);
            session.setTimeout(sessionKeyTimeOut);
        }
    }
}
