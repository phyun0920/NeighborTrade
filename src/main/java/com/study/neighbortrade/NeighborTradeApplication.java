package com.study.neighbortrade;

import com.study.neighbortrade.config.MarketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MarketProperties.class)
public class NeighborTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NeighborTradeApplication.class, args);
    }
}
