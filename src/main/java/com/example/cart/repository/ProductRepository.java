package com.example.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.cart.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    // add custom queries if needed
}
