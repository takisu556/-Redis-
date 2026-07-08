package com.example.cart.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId;
    private List<OrderItem> items;
    private double total;
    private Instant createdAt;

    public Order() {}

    public Order(String userId, List<OrderItem> items, double total) {
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class OrderItem {
        private String productId;
        private String name;
        private double price;
        private long quantity;

        public OrderItem() {}

        public OrderItem(String productId, String name, double price, long quantity) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public long getQuantity() { return quantity; }
        public void setQuantity(long quantity) { this.quantity = quantity; }
    }
}
