package com.example.cart.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product implements Serializable {
    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private double price;

    @JsonProperty("quantity")
    private long quantity;

    @JsonProperty("category")
    private String category;

    @JsonProperty("img")
    private String img;

    @JsonProperty("link")
    private String link;

    @JsonProperty("slogan")
    private String slogan;

    public Product() {}

    public Product(String id, String name, double price, long quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public Product(String id, String name, double price, long quantity,
                   String category, String img, String link, String slogan) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.img = img;
        this.link = link;
        this.slogan = slogan;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}
    public String getImg() {return img;}
    public void setImg(String img) {this.img = img;}
    public String getLink() {return link;}
    public void setLink(String link) {this.link = link;}
    public String getSlogan() {return slogan;}
    public void setSlogan(String slogan) {this.slogan = slogan;}

    public Map<String, String> toMap() {
        Map<String, String> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("price", Double.toString(price));
        m.put("quantity", Long.toString(quantity));
        return m;
    }

    public static Product fromMap(Map<String, String> m) {
        if (m == null || m.isEmpty()) return null;
        Product p = new Product();
        p.setId(m.get("id"));
        p.setName(m.get("name"));
        p.setPrice(Double.parseDouble(m.getOrDefault("price", "0")));
        p.setQuantity(Long.parseLong(m.getOrDefault("quantity", "0")));
        return p;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", category='" + category + '\'' +
                ", slogan='" + slogan + '\'' +
                '}';
    }
}