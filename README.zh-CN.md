# mybatis-enhance

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-21-orange)](https://github.com/easy-4-java/mybatis-enhance) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](./LICENSE)

> **项目状态**：`feature/3.0.x` 版本线维护中（JDK 21）。制品尚未发布到 Maven Central，通过项目私服与 GitHub Releases 分发。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本与分支](#10-版本与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`mybatis-enhance` 是面向**原生 MyBatis** 的增强组件。项目只依赖 MyBatis 核心 API，不依赖 Spring、MyBatis-Spring 或 MyBatis-Plus；需要 MyBatis-Plus 专属能力时，应使用独立项目 `mybatis-plus-enhance`。

是什么：

- 统一的 MyBatis 拦截器链（`MybatisEnhanceInterceptor` + `EnhanceInnerInterceptor` SPI）——在同一个插件注册中即可编排写入前加密、写入前签名、查询后验签与查询后解密；
- 独立拦截器——分页（`PaginationInterceptor` + `Dialect`）、`INSERT IGNORE` 改写、长 SQL 检测与 SQL 观测；
- `mybatis-enhance-extension` 中的数据权限（行级）、国际化、字段加解密/签名与字段填充能力，底层使用 JSqlParser 3.1 SQL AST 工具；
- 通用 `TypeHandler` 集合（数组、Fastjson2/Jackson/Hutool JSON、集合、Blob、Date、RSA 模板），也可被 MyBatis-Plus 项目直接复用；
- 基于 Spring 事务的服务语义（`IEnhanceService`），提供"保存/更新后签名"工作流。

不是什么：

- 不是 MyBatis-Plus 的替代品，也不是 Spring Boot Starter；
- 不提供脱敏注解与脱敏拦截器——脱敏属于接口输出/序列化边界（组织内由 `ddd4j-extension-jackson` 的 `@Sensitive` 承担），持久化层只负责可逆加解密、完整性签名与数据权限。

典型场景：

| 场景 | 模块 / 类 |
| :--- | :--- |
| 写入加密、签名，查询验签、解密 | `mybatis-enhance-extension` → `MybatisEnhanceInterceptor` + `DataEncryption/DataSignature/DataDecryptionInnerInterceptor` |
| 行级数据权限（注解 / 脚本 / 自动装配） | `mybatis-enhance-extension` → `datascope` 解析器 + `DefaultDataPermissionStatementInterceptor` |
| 字段级国际化切换 | `mybatis-enhance-extension` → `i18n` 处理器 + `AbstractDataI18nInterceptor` |
| 不引入完整 ORM 的分页 | `mybatis-enhance-core` → `PaginationInterceptor` + `MysqlDialect` |
| JSON / 数组 / Blob 列 | `mybatis-enhance-typehandler` → 如 `JsonTypeHandler`、`ArrayStringTypeHandler` |
| Spring 服务中的签名持久化 | `mybatis-enhance-spring` → `IEnhanceService.saveSigned / updateSignedById` |

## 2. 功能与状态

| 能力 | 状态 | 说明 |
| :--- | :--- | :--- |
| 统一增强拦截器链 | 已实现 | `MybatisEnhanceInterceptor`（Executor update/query）+ `EnhanceInnerInterceptor` SPI（`before/afterUpdate`、`before/afterQuery`、`afterExecution`） |
| 分页 | 已实现 | `PaginationInterceptor` + `Dialect` / `MysqlDialect`（`buildPaginationSql`、`buildCountSql`） |
| `INSERT IGNORE` 改写 | 已实现 | `InsertIgnoreInterceptor`（含全局 enable/reset 开关） |
| 长 SQL 检测 | 已实现 | `LongSqlInterceptor`（阈值 + 处理器） |
| SQL 观测 | 已实现 | `SqlObservationInterceptor` + `SqlObservationSink` / `SqlLoggingSink` |
| 字段加解密 / 签名 | 已实现 | `mybatis-enhance-extension` crypto 处理器 + 内部拦截器 |
| 数据权限（行级） | 已实现 | 基于 JSqlParser AST 的注解 / 脚本 / 自动装配解析器 |
| 国际化列 | 已实现 | `mybatis-enhance-extension` i18n 处理器 + bundle |
| 通用 TypeHandler | 已实现 | `mybatis-enhance-typehandler` 提供 20 个可直接使用的 TypeHandler + 6 个抽象基类（共 26 个类） |
| Spring 服务语义 | 已实现 | `IEnhanceService` / `EnhanceServiceImpl`（Spring 事务） |
| 测试 | 已有 | `MybatisEnhanceInterceptorTest`、加解密/签名集成测试、`PermissionTableVisitorTest` |

## 3. 环境要求与兼容性

| 项目 | 要求 |
| :--- | :--- |
| JDK | 21+ |
| Maven | 3.9.6+（内置 Maven Wrapper `mvnw`） |
| MyBatis | 3.5.17 |
| JSqlParser | 3.1（SQL AST 工具的有意兼容基线） |
| 其他依赖（根 pom 统一管理） | fastjson2 2.0.62、hutool 5.8.40、jackson 2.17.2、commons-lang3、slf4j-api 2.0.18、lombok（provided） |
| 多模块版本管理 | `${revision}` + flatten-maven-plugin |

版本线（各线细节见 [COMPATIBILITY.md](COMPATIBILITY.md)）：

| 分支 | JDK | 版本模式 |
| :--- | :---: | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. 架构与模块

```text
            +---------------------------------------------+
            |  mybatis-enhance-annotation (纯 Java)       |
            |  crypto / i18n / permission 注解            |
            +---------------------+-----------------------+
                                  |
+-----------------+    +----------v-----------+    +---------------------------+
| mybatis-enhance-|    | mybatis-enhance-core |    | mybatis-enhance-extension  |
| typehandler     |    | 拦截器、SPI、         |    | datascope / crypto / i18n /|
| JSON/数组/Blob  |    | 分页、基础工具        |    | 填充 + JSqlParser AST      |
+--------+--------+    +----------+-----------+    +-------------+-------------+
         |                        |                               |
         +------------------------+-------------------------------+
                                  |
                                  v
                           MyBatis 3.5.17
```

五个模块：

| 模块 | 职责 | 可独立使用 |
| :--- | :--- | :--- |
| `mybatis-enhance-annotation` | 数据权限、国际化、加解密和签名等纯 Java 注解 | 是，零运行时依赖 |
| `mybatis-enhance-core` | 原生 MyBatis 拦截器链、SPI（`Dialect`、`Fillable` 等）、独立插件与基础工具 | 是 |
| `mybatis-enhance-extension` | 数据权限、国际化、加解密、签名、字段填充及 JSqlParser SQL AST 增强 | 否，依赖 annotation 与 core |
| `mybatis-enhance-typehandler` | 通用 TypeHandler：JSON、集合、Blob、Date、RSA 模板等 | 是，也可用于 MyBatis-Plus 项目 |
| `mybatis-enhance-spring` | 带签名/验签语义的 Spring 事务 Service API（`IEnhanceService`） | 是，需配合 extension + spring-tx |

注解按能力域组织在 `org.apache.ibatis.enhance.annotation.{crypto,i18n,permission}` 下；其中 crypto 与 i18n 注解可被 `mybatis-plus-enhance` 直接复用（不重复定义）。模块依赖方向：`extension → {annotation, core}`、`spring → extension`、`typehandler` 仅依赖 MyBatis。

## 5. 安装

仅使用通用 TypeHandler：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>mybatis-enhance-typehandler</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

使用完整原生 MyBatis 增强能力：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>mybatis-enhance-extension</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:mybatis-enhance-extension:3.0.x.x.20260630-SNAPSHOT'
```

只需要拦截器链/SPI 时直接依赖 `mybatis-enhance-core`；只需要注解协议时直接依赖 `mybatis-enhance-annotation`。快照版本由项目私服提供（见 pom 中 `distributionManagement`）。尚未发布 Maven Central 正式版。

## 6. 快速开始

装配加密 + 签名内部拦截器的统一增强链（用法取自项目既有文档）：

```java
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.enhance.plugins.MybatisEnhanceInterceptor;
import org.apache.ibatis.enhance.plugins.inner.EnhanceInnerInterceptor;
import org.apache.ibatis.enhance.crypto.handler.EncryptedFieldHandler;
import org.apache.ibatis.enhance.crypto.handler.DataSignatureHandler;
import org.apache.ibatis.enhance.crypto.interceptor.DataEncryptionInnerInterceptor;
import org.apache.ibatis.enhance.crypto.interceptor.DataSignatureInnerInterceptor;
import org.apache.ibatis.enhance.crypto.interceptor.DataDecryptionInnerInterceptor;

EncryptedFieldHandler encryptedFieldHandler = createEncryptedFieldHandler();
DataSignatureHandler signatureHandler = createSignatureHandler();

MybatisEnhanceInterceptor enhanceInterceptor = new MybatisEnhanceInterceptor();
enhanceInterceptor.addInterceptor(new DataEncryptionInnerInterceptor(encryptedFieldHandler));
enhanceInterceptor.addInterceptor(new DataSignatureInnerInterceptor(signatureHandler));
enhanceInterceptor.addInterceptor(new DataDecryptionInnerInterceptor(encryptedFieldHandler));

Configuration configuration = new Configuration();
configuration.addInterceptor(enhanceInterceptor);
```

生命周期语义：写操作执行 `beforeUpdate` 后执行 SQL，再执行 `afterUpdate`；查询操作执行 `beforeQuery` 后执行 SQL，再执行 `afterQuery`；`afterExecution` 是旁路通知，单个增强器异常隔离。内部拦截器严格按注册顺序执行——签名字段依赖密文时，应先注册加密增强器，再注册签名增强器。

## 7. 配置

无应用属性前缀。行为全部在代码中配置：

- `MybatisEnhanceInterceptor.addInterceptor(...)` 决定内部拦截器顺序；
- `LongSqlInterceptor(longSqlThreshold, longSqlHandler)` 阈值；
- `InsertIgnoreInterceptor.enable()/reset()` 全局开关；
- `PaginationInterceptor` 的 `Dialect` 选择（如 `new PaginationInterceptor(new MysqlDialect())`）；
- crypto 密钥材料通过 `CryptoKeyMaterial` / `StaticCryptoKeyProvider` 与处理器（`DataEncryptionHandler`、`DataSignatureHandler`、`EncryptedFieldHandler`）；
- 租户/国际化上下文通过 `TenantContext` 与 `DataInputProvider`。

## 8. 核心用法 / API

### 8.1 分页拦截器

```java
import org.apache.ibatis.enhance.plugins.PaginationInterceptor;
import org.apache.ibatis.enhance.spi.MysqlDialect;

PaginationInterceptor pagination = new PaginationInterceptor(new MysqlDialect());
configuration.addInterceptor(pagination);
// Dialect 契约：String buildPaginationSql(originalSql, offset, size);
//               String buildCountSql(originalSql);
```

### 8.2 通过 Spring Service 签名持久化

```java
import org.apache.ibatis.enhance.service.IEnhanceService;

IEnhanceService<User> service = ...;

service.saveSigned(user);              // 插入 + 按 ID 签名（Spring @Transactional）
service.updateSignedById(user);        // 更新 + 重新签名
service.saveBatchSigned(userList);     // 批量插入 + 签名（默认批大小 1000）
```

`EnhanceMapper<T>` 提供 `insert / updateById / selectById / selectBatchIds / selectList`，以及跳过解密读取的 `@IgnoreEncrypted` 变体（`selectIgnoreDecryptById`、`selectIgnoreDecryptBatchIds`）。

### 8.3 基于 JSqlParser 的数据权限

`mybatis-enhance-extension` 保留基于 JSqlParser AST 的数据权限改写：`QueryTablesNamesFinder` 提取查询涉及的表；`SelectAutowirePermissionParser`、`SelectAnnotationPermission(s)Parser` 与 `SelectAnnotationSpecialPermission(s)Parser` 应用自动装配 / 注解 / 特殊权限策略；`SqlParserTool` 是通用的 AST 解析与改写入口。Visitor 复用 JSqlParser 官方 `TablesNamesFinder` 的遍历逻辑，本项目只维护权限替换策略，避免多份 Visitor 实现产生行为漂移。

### 8.4 脱敏能力边界（明确不在范围内）

数据脱敏属于序列化边界问题。本项目不会原地修改写入参数、查询结果或缓存对象：持久化层负责可逆加解密、完整性签名与数据权限；表现层负责生成脱敏视图（组织内 `ddd4j-extension-jackson` 提供 `@Sensitive` + `SensitiveStrategy` + `SensitiveJsonSerializer`）。

## 9. 测试与构建

```bash
./mvnw clean verify
```

多模块版本统一由 `${revision}` 管理；`flatten-maven-plugin` 在构建时生成可发布、可消费的 POM。发布：

```bash
./mvnw -Prelease clean deploy -Drevision=<release-version>
```

质量门禁：

- 测试套件：`MybatisEnhanceInterceptorTest`（core）、加解密/签名集成测试（`CryptoSignatureIntegrationTest`、`CryptoHandlersTest`、`CryptoMapper` + `CryptoMapper.xml`）与 `PermissionTableVisitorTest`（extension）；
- JaCoCo 覆盖率报告 + 行覆盖率检查规则，最低目标 90%（`haltOnFailure=false`）；
- package 阶段附加源码包与 Javadoc 包；Maven Enforcer 规则守护构建环境。

## 10. 版本与分支

三条并行版本线，各自绑定一个 JDK 基线：

| 分支 | JDK | 版本模式 | 维护状态 |
| :--- | :---: | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前开发线 |
| `feature/2.0.x` | 17 | `2.0.x.*` | 并行维护 |
| `feature/3.0.x` | 21 | `3.0.x.*` | 并行维护 |

跨版本线同步以能力和公共契约一比一为目标，但依赖版本、编译配置、JSqlParser API 适配与 Java 语法必须分别符合对应 JDK 基线，不能机械复制构建配置（见 [COMPATIBILITY.md](COMPATIBILITY.md)）。本分支快照版本为 `3.0.x.x.20260630-SNAPSHOT`；正式版本通过 GitHub Releases 发布，Maven Central 发布已规划，尚未执行。

## 11. 贡献与许可

欢迎通过 GitHub Issue 或 Pull Request 参与贡献。所有源码基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 许可。
