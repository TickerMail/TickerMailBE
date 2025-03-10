package com.lambda.stocksubscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockSubscriptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockSubscriptionApplication.class, args);
    }

}
