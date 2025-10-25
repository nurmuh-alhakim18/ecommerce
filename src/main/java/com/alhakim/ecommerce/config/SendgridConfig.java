package com.alhakim.ecommerce.config;

import com.sendgrid.SendGrid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@RequiredArgsConstructor
public class SendgridConfig {
    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Bean
    public SendGrid sendgrid() {
        return new SendGrid(apiKey);
    }
}
