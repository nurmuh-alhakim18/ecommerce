package com.alhakim.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.alhakim.ecommerce.entity.Category;
import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.ActivityType;
import com.alhakim.ecommerce.model.ProductDocument;
import com.alhakim.ecommerce.repository.CategoryRepository;
import com.alhakim.ecommerce.repository.ProductCategoryRepository;
import com.alhakim.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkReindexServiceImpl implements BulkReindexService {

    private final ElasticsearchClient elasticsearchClient;
    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductIndexService productIndexService;
    private final UserActivityService userActivityService;

    private static final Integer BATCH_SIZE = 100;

    @Override
    @Transactional(readOnly = true)
    @Async
    public void reindexAllProducts() throws IOException {
        long startTime = System.currentTimeMillis();
        AtomicLong totalIndexed = new AtomicLong();
        List<ProductDocument> batch = new ArrayList<>(BATCH_SIZE);

        try (Stream<Product> products = productRepository.streamAll()) {
            products.forEach(product -> {
               List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(product.getProductId())
                       .stream().map(productCategory -> productCategory.getId().getCategoryId()).toList();

               List<Category> categories = categoryRepository.findAllById(categoryIds);
                ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categories);
                Long viewCount = userActivityService.getActivityCount(product.getProductId(), ActivityType.VIEW);
                Long purchaseCount = userActivityService.getActivityCount(product.getProductId(), ActivityType.PURCHASE);

                productDocument.setViewCount(viewCount);
                productDocument.setPurchaseCount(purchaseCount);
                batch.add(productDocument);

                if (batch.size() >= BATCH_SIZE) {
                    try {
                        totalIndexed.addAndGet(indexBatch(batch));
                        batch.clear();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        if (!batch.isEmpty()) {
            totalIndexed.addAndGet(indexBatch(batch));
        }

        long endTime = System.currentTimeMillis();
        log.info("Total Indexed: {}. Time taken: {} ms", totalIndexed, endTime - startTime);
    }

    private long indexBatch(List<ProductDocument> batch) throws IOException {
        BulkRequest.Builder builder = getBuilder(batch);

        BulkResponse response = elasticsearchClient.bulk(builder.build());
        if (response.errors()) {
            for (BulkResponseItem item: response.items()) {
                if (item.error() != null) {
                    log.error("Error performing bulk operation: " + item.error().reason());
                }
            }
        }

        return batch.size();
    }

    private BulkRequest.Builder getBuilder(List<ProductDocument> batch) {
        BulkRequest.Builder builder = new BulkRequest.Builder();
        for (ProductDocument productDocument : batch) {
            builder.operations(op -> {
                return op.update(upd -> {
                    return upd.index(productIndexService.indexName())
                            .id(productDocument.getId())
                            .action(act -> {
                                return act.docAsUpsert(true)
                                        .doc(productDocument);
                            });
                });
            });
        }

        return builder;
    }
}
