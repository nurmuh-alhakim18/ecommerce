package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.UserActivityReindex;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductActivityReindexProducerImpl implements ProductActivityReindexProducer {

    private final KafkaTemplate<String, UserActivityReindex> kafkaTemplate;

    @Value("${kafka.topic.user-activity-reindex.name}")
    private String topicName;

    @Override
    public void publishUserActivityReindex(UserActivityReindex message) {
        kafkaTemplate.send(topicName, message);
    }
}
