package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.ProductReindex;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductReindexConsumer {

    private final ProductIndexService productIndexService;
    private final ProductService productService;

    @KafkaListener(topics = "${kafka.topic.product-reindex.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ProductReindex message) {
        if (!List.of("REINDEX", "DELETE").contains(message.getAction())) {
            return;
        }

        Product product = productService.get(message.getProductId());
        if (product == null) {
            return;
        }

        if (message.getAction().equals("REINDEX")) {
            productIndexService.reindexProduct(product);
            return;
        }

        if (message.getAction().equals("DELETE")) {
            productIndexService.deleteProduct(product);
        }
    }
}
