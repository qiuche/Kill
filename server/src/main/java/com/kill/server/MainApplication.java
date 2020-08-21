package com.kill.server;/**
 * Created by Administrator on 2019/6/13.
 */

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author:debug (SteadyJack)
 * @Date: 2019/6/13 22:50
 **/
@SpringBootApplication
@ImportResource(value = {"classpath:spring/spring-jdbc.xml"})
//@ImportResource(value = "classpath:spring/spring-shiro.xml")
@MapperScan(basePackages = "com.kill.model.mapper")
@EnableScheduling
public class MainApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(com.kill.server.MainApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(com.kill.server.MainApplication.class,args);
    }

}