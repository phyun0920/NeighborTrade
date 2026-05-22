package com.study.neighbortrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.market")
public record MarketProperties(List<String> popularKeywords) {
    public MarketProperties {
        popularKeywords = popularKeywords == null ? List.of() : List.copyOf(popularKeywords);
    }
}
