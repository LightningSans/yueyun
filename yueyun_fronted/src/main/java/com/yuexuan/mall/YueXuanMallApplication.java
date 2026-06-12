package com.yuexuan.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YueXuanMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(YueXuanMallApplication.class, args);
    }
}
