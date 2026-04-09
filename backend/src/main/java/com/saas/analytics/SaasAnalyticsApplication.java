package com.saas.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SaasAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasAnalyticsApplication.class, args);
    }

}
