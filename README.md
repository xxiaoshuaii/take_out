# Sky 外卖管理系统

一个基于 Spring Boot 的在线外卖平台后端项目,包含管理端(餐厅运营)与用户端(C 端点餐)两套完整业务,覆盖从员工/菜品管理、订单履约、微信支付到数据统计报表的全链路功能。

> 本项目作为个人后端工程实践,聚焦于 **业务建模、性能优化与中间件整合**,目标是还原一个可上线的外卖系统架构。

---

## 一、技术栈

| 分类 | 选型 |
| --- | --- |
| 基础框架 | Spring Boot 2.7.3 / Spring MVC / Spring Task |
| 持久层 | MyBatis + PageHelper 分页插件 |
| 数据库 | MySQL 8.x + Redis 6.x |
| 连接池 | Alibaba Druid |
| 安全/鉴权 | JWT (jjwt) + 自定义拦截器 |
| 接口文档 | Knife4j / Swagger 2 |
| 文件存储 | 阿里云 OSS |
| 实时通信 | WebSocket(来单/催单提醒) |
| 报表导出 | Apache POI |
| 第三方对接 | 微信登录、微信支付 v3 |
| 其他 | AOP、Lombok、Fastjson、HttpClient |

## 二、项目结构

```
sky-take-out
├── sky-common      // 通用模块:工具类、异常、常量、JWT、OSS、统一返回结果
├── sky-pojo        // 数据对象:Entity / DTO / VO
└── sky-server      // 业务模块:Controller / Service / Mapper / 配置 / 拦截器 / 定时任务
```

采用 **Maven 多模块** 拆分,便于职责隔离与后续微服务化演进。

## 三、核心功能

### 管理端
- **员工管理**:登录鉴权(JWT)、新增/查询/启停员工、密码 MD5 加密
- **分类与菜品管理**:菜品 / 套餐分类、菜品 CRUD、口味关联、起售停售联动校验
- **套餐管理**:套餐与菜品多对多绑定,套餐内菜品停售时联动校验
- **订单管理**:订单查询、派送、完成、取消、拒单、催单
- **数据统计**:营业额、用户、订单、菜品销量 Top10 等多维报表,支持 Excel 导出
- **工作台**:今日订单、营业额、待处理订单等实时数据看板

### 用户端
- **微信登录**:基于 code 换 openid,自动注册用户
- **微信支付**:对接微信支付 v3 完成下单回调
- **购物车 / 地址簿**:基于 Redis 的购物车存储
- **下单 / 历史订单 / 再来一单**:完整 C 端点餐链路
- **WebSocket 来单提醒**:商家端实时收到新订单与用户催单推送

## 四、技术亮点

1. **公共字段自动填充(AOP)**
   通过自定义注解 `@AutoFill` + AOP 切面,统一在 Insert/Update 时填充 `createTime / createUser / updateTime / updateUser`,消除重复模板代码。

2. **JWT + 双拦截器**
   管理端与用户端独立拦截器,基于不同密钥校验 token,避免越权访问。

3. **Redis 缓存优化**
   - 菜品分类、店铺营业状态等高频读数据缓存,降低 DB 压力
   - 菜品修改 / 起售停售时按 key 精准清理,保证缓存一致性
   - 购物车使用 Hash 结构存储,支持高并发读写

4. **Spring Task 定时任务**
   - 每天处理超时未支付订单 → 自动取消
   - 每天凌晨清理"派送中"超时订单 → 自动完结

5. **WebSocket 双向通信**
   商家端长连接,收到新订单 / 催单时即时弹窗,无需轮询。

6. **统一异常处理 & 统一返回结果**
   通过 `@RestControllerAdvice` 全局捕获业务异常,前后端约定 `Result<T>` 标准结构。

7. **多模块 Maven 工程**
   common / pojo / server 解耦,便于复用与后续扩展。

## 五、本地运行

### 环境要求
- JDK 8+
- MySQL 8.x(建好库后导入 sql 脚本)
- Redis 6.x
- Maven 3.6+

### 启动步骤
```bash
# 1. 修改 sky-server/src/main/resources/application-dev.yml 中的
#    数据库、Redis、阿里云 OSS、微信小程序与微信支付相关配置

# 2. 编译打包
mvn clean package -DskipTests

# 3. 启动
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
```

启动后访问接口文档:
```
http://localhost:8080/doc.html
```

## 六、可优化方向(TODO)

- [ ] 引入 RabbitMQ 解耦下单与通知,提升下单 TPS
- [ ] 接入 SkyWalking 做链路追踪
- [ ] 网关 + 服务拆分,演进为 Spring Cloud Alibaba 微服务架构
- [ ] 引入 ElasticSearch 优化菜品搜索
- [ ] 增加单元测试 & GitHub Actions CI

---

> 项目仅用于学习与技术演示,不用于商业用途。
