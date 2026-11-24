# 在线电影票预订系统

基于 Spring Boot 3 + Vue 3 的前后端分离电影票务系统，包含用户端选座购票与管理员端排片、票房统计。

## 功能概览

### 用户端
- **浏览影讯**：按片名/主演搜索、按上映日期区间与影院筛选；查看海报、评分、简介与排片表
- **选座购票**：可视化座位图（可选 / 已选 / 已售 / 情侣座），下单锁座，30 分钟内模拟支付，生成含二维码的电子票
- **个人中心**：我的票夹（待支付 / 已出票 / 已取消 / 已退款 / 已超时）、开场前 2 小时自助退票、观影后评分与文字评价
- **超时释放**：未支付订单每分钟被定时任务回收，座位自动恢复可售

### 管理员端
- **影片管理**：录入/编辑影片，设置上映与下架时间、状态，上传海报
- **影院影厅**：新增影院与影厅，按“排数 × 列数”自动计算座位数
- **排片管理**：选择影片、影厅、开场时间与票价，自动计算散场时间并生成座位，校验影厅时段冲突
- **票房统计**：总票房、今日票房、售出票数、成交订单，近 7 日趋势、影片 TOP 6、影片/影院明细

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 3.5、Spring Data JPA、Spring Security + JWT、Bean Validation |
| 数据库 | MySQL 8/9（`ddl-auto=update` 自动建表） |
| 电子票 | ZXing 二维码（Base64 内联返回） |
| 接口文档 | SpringDoc OpenAPI（Swagger UI） |
| 前端 | Vue 3、Vite、Vue Router、Pinia、Element Plus、ECharts、Day.js、Axios |

## 项目结构

```
Java project/
├─ backend/                     # Spring Boot 后端
│  ├─ src/main/java/com/example/movie/
│  │  ├─ common/                # 统一响应、异常、分页
│  │  ├─ config/                # 安全配置、数据初始化、超时任务
│  │  ├─ controller/            # 用户端与 /api/admin 管理端接口
│  │  ├─ dto/                   # 请求/响应对象
│  │  ├─ entity/                # User/Movie/Cinema/Hall/Showtime/Seat/Order/Review...
│  │  ├─ repository/            # Spring Data JPA
│  │  ├─ security/              # JWT 过滤器与工具
│  │  └─ service/               # 业务逻辑（下单锁座、支付、退票、统计）
│  ├─ src/main/resources/application.yml
│  └─ mvnw.cmd / mvnw           # 自带 Maven Wrapper
├─ frontend/                    # Vue 3 前端
│  └─ src/
│     ├─ api/                   # Axios 接口封装
│     ├─ layouts/               # 管理后台布局
│     ├─ router/                # 路由与登录/管理员守卫
│     ├─ stores/                # Pinia 用户状态
│     └─ views/                 # 首页、详情、选座、订单、个人中心、admin/*
├─ sql/init.sql                 # 仅创建数据库（表由 JPA 自动生成）
├─ start-backend.cmd            # 一键启动后端
└─ start-frontend.cmd           # 一键启动前端
```

## 环境要求

- JDK 21
- Node.js 18+（npm）
- MySQL 8.0+（本项目在 MySQL 9.6 验证）

## 快速开始

### 1. 初始化数据库

确保 MySQL 已启动，然后执行：

```bash
mysql -u root -p < sql/init.sql
```

### 2. 配置数据库账号

默认读取 `root / 1122695QQ`（见 `backend/src/main/resources/application.yml`）。
推荐通过环境变量覆盖，避免改代码：

```bat
set DB_USERNAME=root
set DB_PASSWORD=你的密码
```

### 3. 启动后端（端口 8080）

双击 `start-backend.cmd`，或手动执行：

```bat
cd backend
mvnw.cmd spring-boot:run
```

- 首次启动会自动建表，并初始化演示账号、6 部影片、3 家影院、影厅、未来 3 天排片与座位
- 接口文档：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/api/ping

### 4. 启动前端（端口 5173）

双击 `start-frontend.cmd`，或手动执行：

```bat
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 。开发服务器已将 `/api` 与 `/uploads` 代理到 8080。

## 演示账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 普通用户 | demo | demo123 |
| 管理员 | admin | admin123 |

也可在注册页自行注册普通用户。

## 主要接口

### 认证
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/register` | 注册（自动登录） |
| POST | `/api/auth/login` | 登录，返回 JWT |
| GET | `/api/auth/me` | 当前用户 |

### 影讯
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/movies` | 影片列表（keyword / releaseFrom / releaseTo / cinemaId / page / size） |
| GET | `/api/movies/{id}` | 影片详情 |
| GET | `/api/movies/{id}/showtimes` | 排片（date / cinemaId），含余座 |
| GET | `/api/cinemas` | 影院列表 |

### 购票
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/showtimes/{id}/seats` | 座位图 |
| POST | `/api/orders` | 创建订单并锁座（30 分钟有效） |
| GET | `/api/orders` | 我的订单（status / page / size） |
| GET | `/api/orders/{id}` | 订单详情 |
| POST | `/api/orders/{id}/pay` | 模拟支付并出票 |
| POST | `/api/orders/{id}/cancel` | 取消未支付订单 |
| POST | `/api/orders/{id}/refund` | 退票（开场前 ≥2 小时） |
| GET | `/api/orders/{id}/ticket` | 电子票（含二维码 Base64） |

### 评价
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/reviews?movieId=` | 影片评价列表（公开） |
| GET | `/api/reviews/mine?movieId=` | 我对该影片的评价 |
| GET | `/api/reviews/my` | 我的全部评价 |
| POST | `/api/reviews` | 提交/修改评价（需已支付订单） |

### 管理员（需 ADMIN 角色）
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST | `/api/admin/movies` | 影片列表 / 新增 |
| PUT | `/api/admin/movies/{id}` | 编辑影片 |
| GET/POST | `/api/admin/cinemas` | 影院列表 / 新增 |
| PUT | `/api/admin/cinemas/{id}` | 编辑影院 |
| GET | `/api/admin/cinemas/{id}/halls` | 影厅列表 |
| POST | `/api/admin/halls` | 新增影厅 |
| GET/POST | `/api/admin/showtimes` | 排片查询 / 新增（自动生成座位） |
| GET | `/api/admin/stats/box-office?days=7` | 票房统计 |
| POST | `/api/admin/upload` | 上传海报（jpg/png/webp ≤5MB） |

## 业务规则

- 登录态使用 JWT，默认有效期 24 小时；`/api/admin/**` 仅管理员可访问
- 座位状态：`0 可售 / 1 已售 / 2 锁定`，下单通过条件更新 `0→2` 原子占座，防止重复售座
- 未支付订单 30 分钟后由定时任务置为超时并释放座位
- 退票仅限已出票订单，且距开场不少于 2 小时；退款生成负数流水，座位恢复可售
- 评价需存在该影片的已支付订单；同一用户同一影片仅一条，重复提交视为修改并重算均分

## 配置项

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_USERNAME` | root | 数据库用户名 |
| `DB_PASSWORD` | 1122695QQ | 数据库密码 |
| `JWT_SECRET` | 内置演示密钥 | 生产环境请替换 |
| `UPLOAD_DIR` | ./uploads | 海报上传目录 |

`app.order.pay-timeout-minutes`（默认 30）与 `app.order.refund-advance-hours`（默认 2）可在 `application.yml` 中调整。

## 验证情况

已完成后端接口与浏览器端到端验证，覆盖：注册登录、影片筛选、排片查询、选座下单、并发送座冲突、模拟支付与二维码、取消/超时释放、退票、评价与均分更新、管理员影片/影院/影厅/排片/统计/上传及权限隔离。
