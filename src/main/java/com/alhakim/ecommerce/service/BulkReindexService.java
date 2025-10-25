package com.alhakim.ecommerce.service;

import java.io.IOException;

public interface BulkReindexService {
    void reindexAllProducts() throws IOException;
}
