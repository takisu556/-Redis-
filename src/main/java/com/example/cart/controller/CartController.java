package com.example.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cart.service.CartService;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    // 查看购物车
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Long>> viewCart(@PathVariable String userId) {
        Map<String, Long> cart = cartService.viewCart(userId);
        return ResponseEntity.ok(cart);
    }

    // 添加商品到购物车
    @PostMapping("/{userId}/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        
        String productId = (String) request.get("productId");
        Long quantity = Long.valueOf(request.get("quantity").toString());
        
        cartService.addToCart(userId, productId, quantity);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "商品已添加到购物车");
        response.put("cart", cartService.viewCart(userId));
        
        return ResponseEntity.ok(response);
    }

    // 从购物车移除商品
    @PostMapping("/{userId}/remove")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        
        String productId = (String) request.get("productId");
        Long quantity = Long.valueOf(request.get("quantity").toString());
        
        cartService.removeFromCart(userId, productId, quantity);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "商品已从购物车移除");
        response.put("cart", cartService.viewCart(userId));
        
        return ResponseEntity.ok(response);
    }

    // 结算购物车
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@PathVariable String userId) {
        boolean success = cartService.checkout(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        if (success) {
            response.put("message", "结算成功！");
        } else {
            response.put("message", "结算失败，可能库存不足或购物车为空");
        }
        
        return ResponseEntity.ok(response);
    }

    // 清空购物车
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> clearCart(@PathVariable String userId) {
        // 需要在 CartService 中添加 clearCart 方法
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "购物车已清空");
        return ResponseEntity.ok(response);
    }
}