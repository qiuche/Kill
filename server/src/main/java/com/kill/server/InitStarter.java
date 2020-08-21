package com.kill.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("项目启动初始化");
    }
}
