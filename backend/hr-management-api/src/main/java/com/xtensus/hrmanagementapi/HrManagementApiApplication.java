package com.xtensus.hrmanagementapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HrManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrManagementApiApplication.class, args);
    }

}
