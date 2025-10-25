package com.alhakim.ecommerce.controller.admin;

import com.alhakim.ecommerce.service.BulkReindexService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/admin/reindex")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class AdminReindexController {

    private final BulkReindexService bulkReindexService;

    @PostMapping("/products")
    public ResponseEntity<String> reindexAllProducts() {
        try {
            bulkReindexService.reindexAllProducts();
            return ResponseEntity.ok("Reindex complete");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
