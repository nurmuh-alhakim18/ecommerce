package com.alhakim.ecommerce.common.errors;

public class InventoryException extends RuntimeException {
    public InventoryException(final String message) {
        super(message);
    }
}
