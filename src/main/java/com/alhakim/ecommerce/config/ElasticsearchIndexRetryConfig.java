package com.alhakim.ecommerce.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class ElasticsearchIndexRetryConfig {

    @Value("${elasticsearch.index.retry.max-attempts}")
    private Integer maxAttempts;

    @Value("${elasticsearch.index.retry.wait-duration}")
    private Duration waitDuration;

    @Bean
    public Retry elasticsearchIndexRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryExceptions(IOException.class)
                .build();

        return Retry.of("elasticsearchIndexRetry", config);
    }
}
