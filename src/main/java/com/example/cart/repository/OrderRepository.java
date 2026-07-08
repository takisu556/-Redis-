package com.example.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.cart.model.Order;

public interface OrderRepository extends MongoRepository<Order, String> {

}
