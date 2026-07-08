package com.example.cart.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.cart.RedisManager;
import com.example.cart.model.Order;
import com.example.cart.model.Product;
import com.example.cart.repository.OrderRepository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class CartService {
    private final JedisPool pool;
    private final ProductService productService;
    private final OrderRepository orderRepository;

    public CartService(ProductService productService, OrderRepository orderRepository) {
        this.pool = RedisManager.getPool();
        this.productService = productService;
        this.orderRepository = orderRepository;
    }

    private String cartKey(String userId) { return "cart:" + userId; }

    // 将商品添加到购物车（仅更新购物车的数量）
    public void addToCart(String userId, String productId, long qty) {
        try (Jedis j = pool.getResource()) {
            String ck = cartKey(userId);
            j.hincrBy(ck, productId, qty);
        }
    }

    // 查看购物车：返回 productId -> quantity
    public Map<String, Long> viewCart(String userId) {
        try (Jedis j = pool.getResource()) {
            String ck = cartKey(userId);
            Map<String, String> raw = j.hgetAll(ck);
            Map<String, Long> out = new HashMap<>();
            for (Map.Entry<String, String> e : raw.entrySet()) {
                out.put(e.getKey(), Long.parseLong(e.getValue()));
            }
            return out;
        }
    }

    // 从购物车移除（减少数量或删除）
    public void removeFromCart(String userId, String productId, long qty) {
        try (Jedis j = pool.getResource()) {
            String ck = cartKey(userId);
            String curS = j.hget(ck, productId);
            if (curS == null) return;
            long cur = Long.parseLong(curS);
            long next = cur - qty;
            if (next > 0) {
                j.hset(ck, productId, Long.toString(next));
            } else {
                j.hdel(ck, productId);
            }
        }
    }

    // 结算购物车：检查库存，若充足则扣库存、保存订单并清空购物车
    public boolean checkout(String userId) {
        try (Jedis j = pool.getResource()) {
            String ck = cartKey(userId);
            Map<String, String> cart = j.hgetAll(ck);
            if (cart.isEmpty()) return false;

            // 检查库存
            for (Map.Entry<String, String> e : cart.entrySet()) {
                String pid = e.getKey();
                long need = Long.parseLong(e.getValue());
                Product p = productService.getProduct(pid);
                if (p == null || p.getQuantity() < need) {
                    return false; // 库存不足或商品不存在
                }
            }

            // 扣减库存（在 Mongo 中），并记录已扣减以便回滚
            List<Map.Entry<String, Long>> adjusted = new ArrayList<>();
            for (Map.Entry<String, String> e : cart.entrySet()) {
                String pid = e.getKey();
                long need = Long.parseLong(e.getValue());
                boolean ok = productService.adjustStock(pid, -need);
                if (!ok) {
                    // 回滚已调整的库存
                    for (Map.Entry<String, Long> prev : adjusted) {
                        productService.adjustStock(prev.getKey(), prev.getValue());
                    }
                    return false;
                }
                adjusted.add(Map.entry(pid, need));
            }

            // 保存订单到 MongoDB
            List<Order.OrderItem> items = new ArrayList<>();
            double total = 0.0;
            for (Map.Entry<String, String> e : cart.entrySet()) {
                String pid = e.getKey();
                long qty = Long.parseLong(e.getValue());
                Product p = productService.getProduct(pid);
                double price = (p != null) ? p.getPrice() : 0.0;
                items.add(new Order.OrderItem(pid, p != null ? p.getName() : "", price, qty));
                total += price * qty;
            }
            Order order = new Order(userId, items, total);
            orderRepository.save(order);

            // 清空购物车（Redis）
            j.del(ck);
            return true;
        }
    }
}