package com.alhakim.ecommerce.controller;

import com.alhakim.ecommerce.common.PageUtil;
import com.alhakim.ecommerce.model.*;
import com.alhakim.ecommerce.service.CachedProductAutocompleteService;
import com.alhakim.ecommerce.service.ProductService;
import com.alhakim.ecommerce.service.SearchService;
import com.alhakim.ecommerce.service.UserActivityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class ProductController {

    private final ProductService productService;
    private final SearchService searchService;
    private final UserActivityService userActivityService;
    private final CachedProductAutocompleteService cachedProductAutocompleteService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        ProductResponse productResponse = productService.findProductById(productId);
        if (!Objects.equals(productResponse.getUserId(), userInfo.getUser().getUserId())) {
            userActivityService.trackProductView(productId, userInfo.getUser().getUserId());
        }

        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/{productId}/similar")
    public ResponseEntity<SearchResponse<ProductResponse>> similarProducts(@PathVariable Long productId) {
        SearchResponse<ProductResponse> response = searchService.similarProducts(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse<ProductResponse>> search(@RequestBody ProductSearchRequest request) {
        SearchResponse<ProductResponse> response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam String text) {
        List<String> suggestions = List.of();
        if (text.length() > 2) {
            suggestions = cachedProductAutocompleteService.combinedAutocomplete(text);
        }

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/suggest/ngram")
    public ResponseEntity<List<String>> ngramSuggestion(@RequestParam String text) {
        List<String> suggestions = List.of();
        if (text.length() > 2) {
            suggestions = cachedProductAutocompleteService.getNgramAutocomplete(text);
        }

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/suggest/fuzzy")
    public ResponseEntity<List<String>> fuzzySuggestion(@RequestParam String text) {
        List<String> suggestions = List.of();
        if (text.length() > 2) {
            suggestions = cachedProductAutocompleteService.getFuzzyAutocomplete(text);
        }

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<SearchResponse<ProductResponse>> recommendations(
            @RequestParam(value = "user_activity", defaultValue = "VIEW") ActivityType activityType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        SearchResponse<ProductResponse> response = searchService.userRecommendation(userInfo.getUser().getUserId(), activityType);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse> findAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "product_id,asc") String[] sort,
            @RequestParam(required = false) String name
    ) {
        List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(orders));
        Page<ProductResponse> productResponses;
        if (name != null && !name.isEmpty()) {
            productResponses = productService.findByProductNameAndPageable(name, pageable);
        } else {
            productResponses = productService.findByPage(pageable);
        }

        return ResponseEntity.ok(productService.convertProductPage(productResponses));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        req.setUser(userInfo.getUser());
        ProductResponse productResponse = productService.createProduct(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestBody @Valid ProductRequest req,
            @PathVariable Long productId
    ) {
        ProductResponse productResponse = productService.updateProduct(productId, req);
        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
