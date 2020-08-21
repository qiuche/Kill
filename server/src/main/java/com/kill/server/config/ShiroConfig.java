package com.kill.server.config;

import com.kill.server.service.CustomRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public CustomRealm customRealm(){
        CustomRealm realm=new CustomRealm();
        return realm;
    }

    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm());
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(){
        ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager());
        bean.setLoginUrl("/to/login");
        bean.setUnauthorizedUrl("/unauth");

        Map<String, String> filterChainDefinitionMap=new HashMap<>();
        filterChainDefinitionMap.put("/to/login","anon");
        filterChainDefinitionMap.put("/**","anon");

        filterChainDefinitionMap.put("/kill/execute","authc");
        filterChainDefinitionMap.put("/item/detail/*","authc");

        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
    }

}
