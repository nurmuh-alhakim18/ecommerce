package com.alhakim.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.alhakim.ecommerce.entity.Category;
import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.ActivityType;
import com.alhakim.ecommerce.model.ProductDocument;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexServiceImpl implements ProductIndexService {

    private static final String INDEX_NAME = "products";

    private final ElasticsearchClient elasticsearchClient;
    private final CategoryService categoryService;
    private final Retry elasticsearchIndexRetry;

    @Override
    @Async
    public void reindexProduct(Product product) {
        List<Category> categories = categoryService.getProductCategories(product.getProductId());
        ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categories);

        IndexRequest<ProductDocument> request = IndexRequest.of(builder -> {
           return builder.index(INDEX_NAME)
                   .id(String.valueOf(product.getProductId()))
                   .document(productDocument);
        });

        try {
            elasticsearchIndexRetry.executeCallable(() -> {
                elasticsearchClient.index(request);
                return null;
            });
        } catch (IOException ex) {
            log.error("Error reindex with id " + product.getProductId() + ": " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Async
    public void deleteProduct(Product product) {
        DeleteRequest deleteRequest = DeleteRequest.of(builder -> {
            return builder.index(INDEX_NAME).id(String.valueOf(product.getProductId()));
        });

        try {
            elasticsearchIndexRetry.executeCallable(() -> {
                elasticsearchClient.delete(deleteRequest);
                return null;
            });
        } catch (IOException ex) {
            log.error("Error delete with id " + product.getProductId() + ": " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String indexName() {
        return INDEX_NAME;
    }

    @Override
    public void reindexProductActivity(Long productId, ActivityType activityType, Long value) {
        final String field = (activityType == ActivityType.VIEW) ? "viewCount" : "purchaseCount";

        UpdateRequest<ProductDocument, Map<String, Object>> request = UpdateRequest.of(u ->
                u.index(INDEX_NAME)
                        .id(String.valueOf(productId))
                        .doc(Map.of(field, value)));

        try {
            elasticsearchIndexRetry.executeCallable(() -> {
                elasticsearchClient.update(request, ProductDocument.class);
                return null;
            });
        } catch (IOException ex) {
            log.error("Error update with id " + productId + ": " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
