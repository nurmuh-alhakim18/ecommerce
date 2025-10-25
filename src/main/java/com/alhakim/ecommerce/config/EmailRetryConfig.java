package com.alhakim.ecommerce.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class EmailRetryConfig {
    @Value("${email.retry.max-attemps}")
    private int maxAttempts;

    @Value("${email.retry.wait-duration}")
    private Duration waitDuration;

    @Bean
    public Retry emailRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryExceptions(IOException.class)
                .build();

        return Retry.of("emailRetry", config);
    }
}
