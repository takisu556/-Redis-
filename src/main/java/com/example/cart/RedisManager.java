package com.example.cart;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {
//单例Jedispool
    private static JedisPool pool;
    // 内存布隆过滤器实例（可选），用于快速判断可能存在的 id，避免穿透
    private static BloomFilter bloom;

//获取jedispool实例
    public static synchronized JedisPool getPool() {
        if (pool == null) {
            String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
            int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
            JedisPoolConfig cfg = new JedisPoolConfig();
            cfg.setMaxTotal(50);
            pool = new JedisPool(cfg, host, port);
        }
        return pool;
    }

//获取jedis实例    
    public static Jedis getJedis(){
        return getPool().getResource();
    }

//关闭jedis链接
    public static void close() {
        if (pool != null) {
            pool.close();
        }
    }

    // ========== Bloom Filter helpers ==========
    // 延迟初始化布隆过滤器
    public static synchronized void initBloom(int expectedElements, double fpp) {
        if (bloom == null) {
            bloom = new BloomFilter(expectedElements, fpp);
        }
    }

    public static void bloomAdd(String value) {
        if (value == null) return;
        if (bloom == null) initBloom(1000, 0.01);
        bloom.add(value);
    }

    public static boolean bloomMightContain(String value) {
        if (value == null) return false;
        if (bloom == null) return true; // 保守：若未初始化，返回 true 以继续查 redis/db
        return bloom.mightContain(value);
    }

    // 内置的简单内存 BloomFilter 实现（FNV-1a 64 + 双哈希）
    private static class BloomFilter {
        private final java.util.BitSet bits;
        private final int bitSize;
        private final int hashFunctions;

        BloomFilter(int expectedElements, double fpp) {
            if (expectedElements <= 0) expectedElements = 1000;
            if (fpp <= 0) fpp = 0.01;
            double m = - (expectedElements * Math.log(fpp)) / (Math.pow(Math.log(2), 2));
            this.bitSize = (int) Math.ceil(m);
            double k = (m / expectedElements) * Math.log(2);
            this.hashFunctions = Math.max(1, (int) Math.round(k));
            this.bits = new java.util.BitSet(bitSize);
        }

        void add(String value) {
            if (value == null) return;
            long h1 = fnv1a64(value);
            long h2 = fnv1a64(new StringBuilder(value).reverse().toString());
            for (int i = 0; i < hashFunctions; i++) {
                long combined = (h1 + i * h2) & 0x7fffffffffffffffL;
                int idx = (int) (combined % bitSize);
                bits.set(idx);
            }
        }

        boolean mightContain(String value) {
            if (value == null) return false;
            long h1 = fnv1a64(value);
            long h2 = fnv1a64(new StringBuilder(value).reverse().toString());
            for (int i = 0; i < hashFunctions; i++) {
                long combined = (h1 + i * h2) & 0x7fffffffffffffffL;
                int idx = (int) (combined % bitSize);
                if (!bits.get(idx)) return false;
            }
            return true;
        }

        // FNV-1a 64-bit
        private long fnv1a64(String data) {
            final long FNV_64_PRIME = 0x100000001b3L;
            long hash = 0xcbf29ce484222325L;
            for (int i = 0; i < data.length(); i++) {
                hash ^= data.charAt(i);
                hash *= FNV_64_PRIME;
            }
            return hash;
        }
    }
}