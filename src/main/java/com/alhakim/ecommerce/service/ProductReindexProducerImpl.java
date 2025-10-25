package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.ProductReindex;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductReindexProducerImpl implements ProductReindexProducer {

    private final KafkaTemplate<String, ProductReindex> kafkaTemplate;

    @Value("${kafka.topic.product-reindex.name}")
    private String topicName;

    @Override
    public void publishProductReindex(ProductReindex message) {
        kafkaTemplate.send(topicName, message);
    }
}
