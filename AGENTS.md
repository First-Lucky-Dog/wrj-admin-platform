# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 项目概述

这是一个基于芋道（Yudao）开源框架的 Spring Boot + Vue3 全栈项目，采用前后端分离架构。

**技术栈：**
- 后端：Spring Boot 2.7.18 + MyBatis Plus + MySQL 8.x + Redis
- 前端：Vue 3.5.12 + Vite 5.1.4 + Element Plus 2.11.1 + TypeScript 5.3.3
- Java 版本：JDK 1.8
- 包管理：Maven (后端) + pnpm (前端)

## 常用命令

### 后端开发

```bash
# 编译整个项目
mvn clean install -DskipTests

# 运行后端服务（在 yudao-server 目录）
mvn spring-boot:run

# 启动类位置
# d:\project\wrj\yudao-server\src\main\java\cn\iocoder\yudao\server\YudaoServerApplication.java

# 数据库初始化脚本
# d:\project\wrj\sql\mysql\ruoyi-vue-pro.sql
# d:\project\wrj\sql\mysql\quartz.sql
```

### 前端开发

```bash
# 进入前端目录
cd yudao-ui

# 安装依赖
pnpm install

# 本地开发（连接本地后端）
pnpm dev

# 连接开发服务器
pnpm dev-server

# 构建生产环境
pnpm build:prod

# 构建本地环境
pnpm build:local

# 代码格式化
pnpm lint:format

# ESLint 检查
pnpm lint:eslint
```

## 项目架构

### 后端模块结构

```
yudao/
├── yudao-dependencies/          # 依赖版本管理
├── yudao-framework/             # 框架封装层
│   ├── yudao-common/           # 通用工具类
│   ├── yudao-spring-boot-starter-mybatis/    # MyBatis 封装
│   ├── yudao-spring-boot-starter-redis/      # Redis 封装
│   ├── yudao-spring-boot-starter-security/   # 安全认证封装
│   ├── yudao-spring-boot-starter-web/        # Web 封装
│   └── ...                     # 其他 starter
├── yudao-module-system/         # 系统管理模块（用户、角色、权限、菜单等）
├── yudao-module-infra/          # 基础设施模块（配置、文件、代码生成等）
├── yudao-module-bpm/            # 工作流模块（Flowable）
├── yudao-module-iot/            # 物联网模块
├── yudao-module-ai/             # AI 大模型模块
├── yudao-module-*/              # 其他业务模块（默认注释，按需启用）
└── yudao-server/                # 主启动模块（空壳容器）
```

**模块启用方式：**
- 在 `pom.xml` 根目录取消注释对应的 `<module>` 标签
- 在 `yudao-server/pom.xml` 中取消注释对应的 `<dependency>` 标签

### 前端目录结构

```
yudao-ui/src/
├── api/                # API 接口定义
├── assets/             # 静态资源
├── components/         # 公共组件
├── layout/             # 布局组件
├── router/             # 路由配置
├── store/              # Pinia 状态管理
├── styles/             # 全局样式
├── utils/              # 工具函数
├── views/              # 页面视图
│   ├── system/        # 系统管理页面
│   ├── infra/         # 基础设施页面
│   ├── uav/           # 无人机模块页面（自定义）
│   └── ...
└── types/              # TypeScript 类型定义
```

### 后端分层架构

每个业务模块遵循标准三层架构：

```
yudao-module-xxx/src/main/java/cn/iocoder/yudao/module/xxx/
├── controller/         # 控制器层（RESTful API）
│   └── admin/         # 管理后台接口
│       └── vo/        # View Object（请求/响应对象）
├── service/           # 业务逻辑层
│   └── impl/          # 实现类
├── dal/               # 数据访问层
│   ├── dataobject/    # DO（数据库实体）
│   └── mapper/        # MyBatis Mapper
└── convert/           # 对象转换（MapStruct）
```

**关键约定：**
- Controller 只做参数校验和调用 Service
- Service 包含业务逻辑，可调用其他 Service
- Mapper 只做数据库操作
- 使用 MapStruct 进行 VO/DO 转换
- 统一返回 `CommonResult<T>` 包装响应

## 配置文件

- **主配置：** `yudao-server/src/main/resources/application.yaml`
- **环境配置：**
  - `application-local.yaml` - 本地开发
  - `application-dev.yaml` - 开发环境
- **前端环境：** `yudao-ui/.env.*` 文件

**数据库配置位置：** 在 `application-local.yaml` 中配置数据源

## 代码生成

框架内置代码生成器，可快速生成 CRUD 代码：

1. 访问：系统管理 -> 代码生成
2. 导入数据库表
3. 配置生成选项
4. 下载生成的代码
5. 将代码复制到对应模块

**生成内容：**
- 后端：Controller、Service、Mapper、DO、VO
- 前端：Vue 页面、API 接口、路由配置

## 开发规范

### 后端规范

1. **包命名：** `cn.iocoder.yudao.module.{模块名}.{分层}`
2. **类命名：**
   - Controller: `XxxController`
   - Service: `XxxService` / `XxxServiceImpl`
   - Mapper: `XxxMapper`
   - DO: `XxxDO`
   - VO: `XxxRespVO` / `XxxReqVO` / `XxxPageReqVO`
3. **数据库字段：**
   - 必须包含：`creator`, `create_time`, `updater`, `update_time`, `deleted`
   - 主键统一使用 `id BIGINT`
   - 逻辑删除：`deleted` (0=未删除, 1=已删除)
4. **API 路径：** `/admin-api/{模块名}/{业务名}/{操作}`

### 前端规范

1. **文件命名：** 使用 kebab-case（小写短横线）
2. **组件命名：** 使用 PascalCase
3. **API 调用：** 统一通过 `src/api/` 中定义的接口
4. **路由配置：** 在 `src/router/modules/` 中按模块组织
5. **权限控制：** 使用 `v-hasPermi` 指令

## 无人机 Demo 相关

项目中已包含无人机模块的数据库设计：

**数据库脚本：** `d:\project\wrj\sql脚本\demo.sql`

**核心表结构：**
- `uav_device` - 无人机设备表
- `uav_route` - 航线模板表
- `uav_route_point` - 航线点位表
- `uav_mission` - 飞行任务表
- `uav_track_point` - 飞行轨迹点表
- `uav_command_log` - 控制指令日志表

**前端视图目录：** `yudao-ui/src/views/uav/`（需创建）

## 注意事项

1. **文件路径：** Windows 环境下使用完整绝对路径（如 `d:\project\wrj\...`）
2. **编码格式：** 统一使用 UTF-8 无 BOM
3. **多租户：** 框架默认启用多租户，新表需考虑租户隔离
4. **数据权限：** 使用 `@DataPermission` 注解控制数据范围
5. **API 文档：** 启动后访问 `http://localhost:48080/doc.html` (Knife4j)
6. **模块依赖：** 新增业务模块需在 `yudao-server` 中添加依赖才能生效
