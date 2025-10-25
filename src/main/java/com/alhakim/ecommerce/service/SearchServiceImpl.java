package com.alhakim.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.json.JsonData;
import com.alhakim.ecommerce.entity.UserActivity;
import com.alhakim.ecommerce.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ProductIndexService productIndexService;
    private final ProductService productService;
    private final UserActivityService userActivityService;

    private static final int SIMILAR_PRODUCT_COUNT = 10;
    private static final int USER_RECOMMENDATION_LIMIT = 10;

    @Override
    public SearchResponse<ProductResponse> search(ProductSearchRequest searchRequest) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            boolQuery.must(MultiMatchQuery.of(mm ->
                            mm.fields("name", "description")
                                    .query(searchRequest.getQuery()))
                    ._toQuery());
        }

        if (searchRequest.getCategory() != null && !searchRequest.getCategory().isEmpty()) {
            Query nestedQuery = NestedQuery.of(n ->
                    n.path("categories")
                            .query(q ->
                                    q.term(t ->
                                            t.field("categories.name.keyword")
                                                    .value(searchRequest.getCategory()))))
                    ._toQuery();

            boolQuery.filter(nestedQuery);
        }

        if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null) {
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder().field("price");
            if (searchRequest.getMinPrice() != null) {
                rangeQuery.gte(JsonData.of(searchRequest.getMinPrice()));
            }

            if (searchRequest.getMaxPrice() != null) {
                rangeQuery.lte(JsonData.of(searchRequest.getMaxPrice()));
            }

            boolQuery.filter(rangeQuery.build()._toQuery());
        }
        
        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
                f.query(q -> q.bool(boolQuery.build()))
                        .functions(
                                FunctionScore.of(fs ->
                                        fs.fieldValueFactor(fvf ->
                                                fvf.field("viewCount")
                                                        .factor(1.0)
                                                        .modifier(FieldValueFactorModifier.Log1p))),
                                FunctionScore.of(fs ->
                                        fs.fieldValueFactor(fvf ->
                                                fvf.field("purchaseCount")
                                                        .factor(2.0)
                                                        .modifier(FieldValueFactorModifier.Log1p))))
                        .boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Sum));

        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(productIndexService.indexName())
                .query(functionScoreQuery._toQuery());

        requestBuilder.sort(s ->
                s.field(f -> f.field(searchRequest.getSortBy())
                        .order("asc".equals(searchRequest.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc)));

        requestBuilder.from((searchRequest.getPage() - 1) * searchRequest.getSize()).size(searchRequest.getSize());
        requestBuilder.aggregations("categories", a ->
                a.nested(n ->
                        n.path("categories"))
                        .aggregations("categories", sa ->
                                sa.terms(t -> t.field("categories.name.keyword"))))
                .from(searchRequest.getPage() - 1);

        SearchRequest elasticRequest = requestBuilder.build();
        SearchResponse<ProductResponse> response;
        try {
            co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result = elasticsearchClient.search(elasticRequest, ProductDocument.class);
            response = mapSearchResult(result);
        } catch (IOException e) {
            log.error("Error performing search: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public SearchResponse<ProductResponse> similarProducts(Long productId) {
        ProductResponse sourceProduct = productService.findProductById(productId);
        List<String> categoryNames = sourceProduct.getCategories().stream().map(CategoryResponse::getName).toList();

        MoreLikeThisQuery moreLikeThisQuery = MoreLikeThisQuery.of(m ->
                m.fields("name", "description")
                        .like(l ->
                                l.document(d ->
                                        d.index(productIndexService.indexName())
                                                .id(productId.toString())))
                .minTermFreq(1)
                .maxQueryTerms(12)
                .minDocFreq(1));

        List<FieldValue> categoryNameFieldValues = categoryNames.stream().map(FieldValue::of).toList();
        NestedQuery nestedQuery = NestedQuery.of(n ->
                n.path("categories")
                        .query(q ->
                                q.terms(t ->
                                        t.field("categories.name")
                                                .terms(t2 -> t2.value(categoryNameFieldValues))))
                        .scoreMode(ChildScoreMode.Avg));

        BoolQuery boolQuery = BoolQuery.of(b ->
                b.must(m -> m.moreLikeThis(moreLikeThisQuery))
                        .should(s -> s.nested(nestedQuery)));

        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
                f.query(q -> q.bool(boolQuery))
                        .functions(
                                FunctionScore.of(fs ->
                                        fs.fieldValueFactor(fvf ->
                                                fvf.field("viewCount")
                                                        .factor(1.0)
                                                        .modifier(FieldValueFactorModifier.Log1p))),
                                FunctionScore.of(fs ->
                                        fs.fieldValueFactor(fvf ->
                                                fvf.field("purchaseCount")
                                                        .factor(2.0)
                                                        .modifier(FieldValueFactorModifier.Log1p))))
                        .boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Sum));

        try {
            co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result = elasticsearchClient.search(s ->
                    s.index(productIndexService.indexName())
                            .query(q -> q.functionScore(functionScoreQuery)).size(SIMILAR_PRODUCT_COUNT)
                    , ProductDocument.class);

            return mapSearchResult(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchResponse<ProductResponse> userRecommendation(Long userId, ActivityType activityType) {
        if (!List.of(ActivityType.PURCHASE, ActivityType.VIEW).contains(activityType)) {
            SearchResponse<ProductResponse> response = new SearchResponse<>();
            response.setFacets(Map.of());
            response.setData(List.of());
            response.setTotalHits(0L);
            return response;
        }

        List<UserActivity> userActivities;
        if (activityType == ActivityType.PURCHASE) {
            userActivities = userActivityService.getLastMonthPurchase(userId);
        } else {
            userActivities = userActivityService.getLastMonthUserView(userId);
        }

        List<Long> topProductIds = getTopProductIds(userActivities);
        return productRecommendationOnActivityType(topProductIds, activityType);
    }

    @Override
    public List<String> getAutocomplete(String query) {
        co.elastic.clients.elasticsearch.core.SearchResponse<Void> response;
        try {
            response = elasticsearchClient.search(s ->
                    s.index(productIndexService.indexName())
                            .suggest(su ->
                                    su.suggesters("name_suggest", fs ->
                                            fs.prefix(query)
                                                    .completion(cs ->
                                                            cs.field("nameSuggest")
                                                                    .skipDuplicates(true)
                                                                    .size(3)))), Void.class);

            return response.suggest().get("name_suggest").stream()
                    .flatMap(s -> s.completion().options().stream())
                    .map(CompletionSuggestOption::text)
                    .toList();
        } catch (IOException e) {
            log.error("Error performing search: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<String> getNgramAutocomplete(String query) {
        co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> response;
        try {
            response = elasticsearchClient.search(s ->
                    s.index(productIndexService.indexName())
                            .query(q ->
                                    q.match(m ->
                                            m.field("nameNgram")
                                                    .query(query)
                                                    .analyzer("ngram_analyzer")))
                            .size(3), ProductDocument.class);

            return response.hits().hits()
                    .stream()
                    .map(hit -> hit.source().getName())
                    .toList();
        } catch (IOException e) {
            log.error("Error performing search: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<String> getFuzzyAutocomplete(String query) {
        co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> response;
        try {
            response = elasticsearchClient.search(s ->
                    s.index(productIndexService.indexName())
                            .query(q ->
                                    q.fuzzy(f ->
                                            f.field("name")
                                                    .value(query)
                                                    .fuzziness("AUTO")))
                            .size(3), ProductDocument.class);

            return response.hits().hits()
                    .stream()
                    .map(hit -> hit.source().getName())
                    .toList();
        } catch (IOException e) {
            log.error("Error performing search: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<String> combinedAutocomplete(String query) {
        List<String> result = new ArrayList<>(getAutocomplete(query));
        if (result.size() < 5) {
            result.addAll(getNgramAutocomplete(query));
        }

        if (result.size() < 5) {
            result.addAll(getFuzzyAutocomplete(query));
        }

        return result.stream()
                .distinct()
                .limit(5)
                .toList();
    }

    private SearchResponse<ProductResponse> productRecommendationOnActivityType(List<Long> productIds, ActivityType activityType) {
        List<Like> moreLikeThisQueries = productIds.stream().map(productId ->
                        Like.of(builder ->
                                builder.document(d -> d.id(String.valueOf(productId)))))
                .toList();

        MoreLikeThisQuery moreLikeThisQuery = MoreLikeThisQuery.of(n ->
                n.fields("name", "description")
                        .like(moreLikeThisQueries)
                        .minTermFreq(1)
                        .maxQueryTerms(12)
                        .minDocFreq(1));

        String fieldName = activityType.equals(ActivityType.PURCHASE) ? "purchaseCount" : "viewCount";
        double factorValue = activityType.equals(ActivityType.PURCHASE) ? 2.0 : 1.0;

        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
                f.query(q -> q.moreLikeThis(moreLikeThisQuery))
                        .functions(FunctionScore.of(fs ->
                                fs.fieldValueFactor(fvf ->
                                        fvf.field(fieldName)
                                                .factor(factorValue)
                                                .modifier(FieldValueFactorModifier.Log1p))))
                        .boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Sum));

        co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> response;
        try {
            response = elasticsearchClient.search(s ->
                    s.index(productIndexService.indexName())
                            .query(q -> q.functionScore(functionScoreQuery))
                            .size(USER_RECOMMENDATION_LIMIT), ProductDocument.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapSearchResult(response);
    }

    private List<Long> getTopProductIds(List<UserActivity> activities) {
        return activities.stream()
                .collect(Collectors.groupingBy(UserActivity::getProductId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    private SearchResponse<ProductResponse> mapSearchResult(co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result) {
        List<ProductResponse> productResponses = result.hits().hits().stream().filter(productDocumentHit ->
                        productDocumentHit != null && productDocumentHit.id() != null)
                .map(productDocumentHit ->
                        Long.parseLong(productDocumentHit.id()))
                .map(productService::findProductById)
                .toList();

        SearchResponse<ProductResponse> response = new SearchResponse<>();
        response.setData(productResponses);
        if (result.hits().total() != null) {
            response.setTotalHits(result.hits().total().value());
        }

        if (result.aggregations() != null) {
            Map<String, List<SearchResponse.FacetEntry>> facets = new HashMap<>();
            var categories = result.aggregations().get("categories");
            if (categories != null && categories.nested() != null) {
                var categoryNameAgg = categories.nested().aggregations().get("category_names");
                if (categoryNameAgg != null && categoryNameAgg.sterms() != null) {
                    List<SearchResponse.FacetEntry> categoryFacets = categoryNameAgg.sterms().buckets().array()
                            .stream()
                            .map(bucket -> new SearchResponse.FacetEntry(bucket.key().stringValue(), bucket.docCount()))
                            .toList();

                    facets.put("categories", categoryFacets);
                }
            }

            response.setFacets(facets);
        }

        return response;
    }
}
