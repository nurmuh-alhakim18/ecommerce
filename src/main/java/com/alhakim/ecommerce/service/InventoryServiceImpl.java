package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.errors.InventoryException;
import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public boolean checkAndLockInventory(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository
                    .findByIdWithPessimisticLocking(entry.getKey())
                    .orElseThrow(() -> new InventoryException("Product not found"));

            if (product.getStockQuantity() < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public void decreaseQuantity(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository
                    .findByIdWithPessimisticLocking(entry.getKey())
                    .orElseThrow(() -> new InventoryException("Product not found"));

            if (product.getStockQuantity() < entry.getValue()) {
                throw new InventoryException("Not enough stock");
            }

            Integer newQuantity = product.getStockQuantity() - entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }
    }

    @Override
    public void increaseQuantity(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository
                    .findByIdWithPessimisticLocking(entry.getKey())
                    .orElseThrow(() -> new InventoryException("Product not found"));

            Integer newQuantity = product.getStockQuantity() + entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }
    }
}
