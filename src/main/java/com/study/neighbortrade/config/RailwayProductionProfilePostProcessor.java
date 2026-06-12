package com.study.neighbortrade.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Railway는 RAILWAY_ENVIRONMENT를 주입한다. SPRING_PROFILES_ACTIVE가 없으면 prod(운영 로그) 프로필을 켠다.
 */
public class RailwayProductionProfilePostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!hasText(environment.getProperty("RAILWAY_ENVIRONMENT"))) {
            return;
        }
        if (hasText(environment.getProperty("SPRING_PROFILES_ACTIVE"))) {
            return;
        }
        environment.addActiveProfile("prod");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
