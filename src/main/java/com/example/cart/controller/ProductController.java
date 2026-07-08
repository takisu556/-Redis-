package com.example.cart.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cart.model.Product;
import com.example.cart.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // 允许跨域请求
public class ProductController {

    @Autowired
    private ProductService productService;

    // 获取所有商品
    @GetMapping
    public ResponseEntity<Set<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.listAll());
    }

    // 根据ID获取商品
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    // 添加或更新商品
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return ResponseEntity.ok("商品添加/更新成功");
    }

    // 删除商品
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        boolean success = productService.deleteProduct(id);
        if (success) {
            return ResponseEntity.ok("商品删除成功");
        }
        return ResponseEntity.badRequest().body("商品不存在或删除失败");
    }

    // 搜索商品（按名称或分类）
    @GetMapping("/search")
    public ResponseEntity<Set<Product>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        // 这里可以扩展 ProductService 添加搜索方法
        return ResponseEntity.ok(productService.listAll());
    }
}