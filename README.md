# Redis 购物车示例（Java + Jedis）

说明：这是一个基于 Redis 哈希实现的简易商品与购物车存储系统，拥有前端 + 后端 + 数据库，网页可视化管理数据与展示商品，使用 Java + Jedis + JavaScript。

前置：
- 需要运行 Redis（本机或 Docker），默认地址 `localhost:6379`，开放8080端口
  - 使用 Docker: `docker run -p 6379:6379 --name redis -d redis:7`。
构建与运行：

运行请于终端输入命令 mvn spring-boot:run ，如若运行失败请检查maven和spring配置环境

- http://localhost:8080/admin.html 为商品添加页面，与数据的可视化查询
- http://localhost:8080/index.html 为购物浏览页面

```bash
重新编译命令
mvn package
java -jar target/redis-cart-1.0-SNAPSHOT-jar-with-dependencies.jar
```

演示：程序提供命令行交互，可添加商品、查询、删除、加入购物车、查看购物车、移除、结算等。

注：代码将商品以 Redis Hash (`product:{id}`) 存储，购物车以 Hash (`cart:{userId}`) 存储（field=productId -> value=quantity）。

对于Redis的缓存雪崩/缓存击穿/缓存穿透三大波及Redis安全性与可用性的危险攻击行为，采取以下措施：

- 缓存击穿
  于 RedisManager.java 文件中添加 Bloom Filter（布隆过滤器），代码来源于 https://blog.csdn.net/isolusion/article/details/146168189，改进误判率；改为延迟启动；添加验证，实现 FNV-1a 64 + 双哈希，缺点是只部署在内存层面且未持久化

- 缓存雪崩
  于 ProductService.java 文件中添加 缓存TTL抖动，代码来源于 https://www.cnblogs.com/lgx211/p/18492169，改进setex字段为随机TTL

- 缓存穿透
  于 ProductSrvice.java 文件中添加 负缓存常量。负缓存可以快速阻止针对不存在 id 的大量重复请求打到数据库，从而有效缓解缓存穿透风险