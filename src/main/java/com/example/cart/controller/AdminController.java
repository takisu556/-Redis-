package com.example.cart.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.cart.model.Product;
import com.example.cart.service.ProductService;

/**
 * 管理员控制器 - 批量操作和数据管理
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ProductService productService;

    /**
     * 批量导入商品
     * POST /api/admin/products/batch
     */
    @PostMapping("/products/batch")
    public ResponseEntity<Map<String, Object>> batchImportProducts(
            @RequestBody List<Product> products) {
        
        int successCount = 0;
        int failCount = 0;
        
        for (Product product : products) {
            try {
                productService.addProduct(product);
                successCount++;
            } catch (Exception e) {
                failCount++;
                System.err.println("导入失败: " + product.getName() + " - " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", products.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message", "批量导入完成");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 清空所有商品（慎用！）
     * DELETE /api/admin/products/all
     */
    @DeleteMapping("/products/all")
    public ResponseEntity<Map<String, Object>> clearAllProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var products = productService.listAll();
            int count = 0;
            for (Product p : products) {
                if (productService.deleteProduct(p.getId())) {
                    count++;
                }
            }
            
            result.put("success", true);
            result.put("deletedCount", count);
            result.put("message", "已清空所有商品");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清空失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 重新初始化数据（添加示例商品）
     * POST /api/admin/products/init
     */
    @PostMapping("/products/init")
    public ResponseEntity<Map<String, Object>> reinitializeProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 初始化数据
            initProducts();
            
            result.put("success", true);
            result.put("total", productService.listAll().size());
            result.put("message", "数据初始化成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "初始化失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 上传商品图片并保存到数据库（以 Base64 存储）
     * POST /api/admin/products/{id}/image
     */
    @PostMapping(value = "/products/{id}/image")
    public ResponseEntity<Map<String, Object>> uploadProductImage(@PathVariable("id") String id,
                                                                  @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String dataUri = productService.uploadImage(id, file);
            if (dataUri != null) {
                result.put("success", true);
                result.put("dataUri", dataUri);
                result.put("message", "图片上传并保存成功");
            } else {
                result.put("success", false);
                result.put("message", "图片上传失败（可能找不到商品或文件为空）");
            }
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "上传处理失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取数据统计
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        var products = productService.listAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", products.size());
        
        long totalStock = products.stream().mapToLong(Product::getQuantity).sum();
        stats.put("totalStock", totalStock);
        
        double totalValue = products.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
        stats.put("totalValue", totalValue);
        
        long categoryCount = products.stream()
                .map(Product::getCategory)
                .distinct()
                .count();
        stats.put("categoryCount", categoryCount);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 查询历史订单
     * GET /api/admin/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<Object> listOrders() {
        if (orderRepository == null) return ResponseEntity.ok(new java.util.ArrayList<>());
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // helper: load orders via repository (injected lazily)
    @Autowired(required = false)
    private com.example.cart.repository.OrderRepository orderRepository;
    
    // ========== 初始化商品数据 ==========
    
    private void initProducts() {
        // Xiaomi 系列
        addProduct("1", "Xiaomi 15S Pro", 5499, 100, "Xiaomi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202505221337_64140259a92b163ab9dc6a7555b31178.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=21305",
            "小米 15周年特别设计");
        
        addProduct("2", "Xiaomi Civil 5 Pro", 2999, 80, "Xiaomi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202505201429_30b6018560c07bc7670cc3ac79070a0c.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=21298",
            "徕卡光学Summilux高速镜头|6000...");
        
        addProduct("3", "Xiaomi 15 钻石限定版", 5999, 50, "Xiaomi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202410282149_a273c623630a535ea738abb3aa0e33ff.png",
            "https://www.mi.com/shop/buy/detail?product_id=20618",
            "徕卡光学 | 骁龙8至尊版");
        
        addProduct("4", "Xiaomi 15 Ultra", 6499, 30, "Xiaomi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202502251810_1d343647c6a9639566f7b4d0ff498f3b.png",
            "https://www.mi.com/shop/buy/detail?product_id=20982",
            "徕卡1英寸主摄|莱卡2亿超级长焦|徕卡...");
        
        // Redmi 系列
        addProduct("6", "REDMI K80", 2299, 120, "Redmi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202411261112_496adbd3fa76742689e9b8f5c4251efc.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20790",
            "第三代骁龙8|2k新国屏|6550mAh超大电...");
        
        addProduct("7", "REDMI Turbo 4 Pro", 1999, 150, "Redmi",
            "https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/5ed659adaedf3b33ed141930314e8bd1.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=21136",
            "Turbo 4 Pro好看，更能打！");
        
        addProduct("8", "Redmi Turbo 4", 1699, 200, "Redmi",
            "https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/00eb27ead9512a7f12604ad3469460b6.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20844",
            "REDMI Turbo 4 好看又能打");
        
        addProduct("9", "Redmi 14C", 469, 300, "Redmi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202412271148_a4cb76ca76ad26ac81da44ad1a69ebd1.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20866",
            "【持久续航】 5160mAh大电池");
        
        addProduct("10", "Redmi K80 Pro", 3299, 90, "Redmi",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202411261127_8d9eefc4ea604b3c2c20ef4df1312591.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20779",
            "骁龙8至尊版|2k新国屏|全焦段影像");
        
        // 耳机系列
        addProduct("11", "Xiaomi Buds5 Pro", 1249, 60, "Earpods",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202502261429_9c6e3418562ed4404309aac4038abd2a.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20978",
            "小米首发圈磁同轴三单元 | 55dB深度...");
        
        addProduct("12", "REDMI Buds 6 Pro", 399, 100, "Earpods",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202411261600_4ae42d79b9b21e0727c1bb7a2cddbbf6.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20796",
            "42dB主动降噪 | 双麦AI通话");
        
        addProduct("13", "Redmi Buds 6 青春版", 139, 150, "Earpods",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202410112149_8c1b7e0aa8f1aca565ca4da1b3f609e6.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20139",
            "小巧轻盈 | 30小时超长续航");
        
        // 手表系列
        addProduct("5", "REDMI Watch 5", 549, 80, "Watch",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202411261459_d47c23aa207b6cb0e2e6feee7d9888c5.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20794",
            "2.07英寸高刷高亮大屏|24天超长续...");
        
        addProduct("14", "Xiaomi Watch S4", 1299, 70, "Watch",
            "https://cdn.cnbj1.fds.api.mi-img.com/mi-mall/15bd8044e1dbe5cdc05806ef3dc7bdf6.png?thumb=1&w=351&h=921&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20588",
            "百变表圈|旋转表冠|小米澎湃OS2|...");
        
        addProduct("15", "Xiaomi Watch S4 Sport", 699, 100, "Watch",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202407180024_944832315a706120a005947cbd642dfc.png?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20140",
            "一体钛机身|蓝宝石玻璃|颂拓运动算法|...");
        
        addProduct("16", "小米手环9 Pro", 399, 120, "Watch",
            "https://cdn.cnbj1.fds.api.mi-img.com/nr-pub/202410291455_6a011d078acca2c8d21b5ece4e680206.jpg?thumb=1&w=300&h=300&f=webp&q=90",
            "https://www.mi.com/shop/buy/detail?product_id=20580",
            "超窄四等边大屏|金属机身|21天超长...");
    }
    
    private void addProduct(String id, String name, double price, long quantity, 
                           String category, String img, String link, String slogan) {
        Product product = new Product(id, name, price, quantity);
        product.setCategory(category);
        product.setImg(img);
        product.setLink(link);
        product.setSlogan(slogan);
        productService.addProduct(product);
    }
}