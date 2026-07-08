package com.example.cart.service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.cart.RedisManager;
import com.example.cart.model.Product;
import com.example.cart.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class ProductService {
    private static final String PRODUCT_SET = "products";
    private static final int CACHE_TTL = 300; // seconds
    private static final int CACHE_JITTER = 60; // seconds, 抖动范围 [0, CACHE_JITTER]
    private static final String NEGATIVE_CACHE_VALUE = "__NULL__";
    private static final int NEGATIVE_CACHE_TTL = 60; 

    private final JedisPool pool;
    private final ProductRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductService(ProductRepository repository) {
        this.pool = RedisManager.getPool();
        this.repository = repository;
        // optional: initialize RedisManager bloom with defaults
        RedisManager.initBloom(1000, 0.01);
        // load existing ids into RedisManager bloom
        for (Product p : repository.findAll()) {
            if (p.getId() != null) RedisManager.bloomAdd(p.getId());
        }
    }

    private String keyFor(String id) { return "product:" + id; }

    // 添加或更新商品
    public void addProduct(Product p) {
        // 写入 MongoDB
        repository.save(p);

        // 更新 Redis 缓存（cache-aside）
        try (Jedis j = pool.getResource()) {
            String k = keyFor(p.getId());
            try {
                String json = mapper.writeValueAsString(p);
                j.setex(k, computeTtl(), json);
                j.sadd(PRODUCT_SET, p.getId());
                if (p.getId() != null) RedisManager.bloomAdd(p.getId());
            } catch (IOException ex) {
                // 序列化失败：记录日志（此处简化为打印）
                ex.printStackTrace();
            }
        }
    }

    public Product getProduct(String id) {
        if (id == null) return null;
        String k = keyFor(id);

        // Bloom filter check via RedisManager: if definitely not present, skip cache/db
        if (!RedisManager.bloomMightContain(id)) {
            return null;
        }

        // 先查 Redis
        try (Jedis j = pool.getResource()) {
            String json = j.get(k);
            if (json != null) {
                // 识别负缓存占位符
                if (NEGATIVE_CACHE_VALUE.equals(json)) {
                    return null;
                }
                try {
                    return mapper.readValue(json, Product.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    // 反序列化失败，继续从 Mongo 查询
                }
            }
        }

        // Redis 未命中，访问 MongoDB
        Optional<Product> opt = repository.findById(id);
        if (opt.isPresent()) {
            Product p = opt.get();
            // 回填 Redis
            try (Jedis j = pool.getResource()) {
                try {
                    j.setex(k, computeTtl(), mapper.writeValueAsString(p));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return p;
        }
        // DB 中不存在：写入短期负缓存以防缓存穿透
        try (Jedis j = pool.getResource()) {
            j.setex(k, NEGATIVE_CACHE_TTL, NEGATIVE_CACHE_VALUE);
        }
        return null;
    }

    private int computeTtl() {
        return CACHE_TTL + ThreadLocalRandom.current().nextInt(0, CACHE_JITTER + 1);
    }

    public boolean deleteProduct(String id) {
        // 删除 MongoDB
        repository.deleteById(id);

        try (Jedis j = pool.getResource()) {
            String k = keyFor(id);
            Long r = j.del(k);
            j.srem(PRODUCT_SET, id);
            // 不能从简单布隆过滤器中移除项（若使用），需额外策略
            return r > 0;
        }
    }

    // 上传图片并保存为 Base64 数据 URI 到 Product.img 字段
    public String uploadImage(String id, MultipartFile file) throws IOException {
        if (id == null || file == null || file.isEmpty()) return null;
        Optional<Product> opt = repository.findById(id);
        if (opt.isEmpty()) return null;
        Product p = opt.get();
        byte[] bytes = file.getBytes();
        String b64 = Base64.getEncoder().encodeToString(bytes);
        String contentType = file.getContentType();
        String dataUri = (contentType != null ? "data:" + contentType + ";base64," : "data:application/octet-stream;base64,") + b64;
        p.setImg(dataUri);
        repository.save(p);

        // 更新缓存
        try (Jedis j = pool.getResource()) {
            try {
                j.setex(keyFor(id), computeTtl(), mapper.writeValueAsString(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dataUri;
    }

    public Set<Product> listAll() {
        // 直接从 MongoDB 列表全部（可按需改为分页）
        Set<Product> out = new HashSet<>();
        for (Product p : repository.findAll()) {
            out.add(p);
        }
        return out;
    }

    // 调整库存（delta 可为负数），返回是否成功（如果会导致库存为负则失败）
    public synchronized boolean adjustStock(String id, long delta) {
        if (id == null) return false;
        Optional<Product> opt = repository.findById(id);
        if (opt.isEmpty()) return false;
        Product p = opt.get();
        long cur = p.getQuantity();
        long next = cur + delta;
        if (next < 0) return false;
        p.setQuantity(next);
        repository.save(p);
        // 更新缓存
        try (Jedis j = pool.getResource()) {
            try {
                j.setex(keyFor(id), computeTtl(), mapper.writeValueAsString(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}