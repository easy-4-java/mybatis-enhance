# mybatis-enhance

[English](./README.md) | [简体中文](./README.zh-CN.md)

> **Status**: maintained on the `feature/3.0.x` line (JDK 21). Artifacts are not yet published to Maven Central; they are distributed through the project's private repository and GitHub Releases.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`mybatis-enhance` is an enhancement component set for **plain MyBatis**. It depends only on MyBatis core APIs — not on Spring, MyBatis-Spring or MyBatis-Plus. If you need MyBatis-Plus-specific capabilities, use the separate `mybatis-plus-enhance` project instead.

What it is:

- A unified MyBatis interceptor chain (`MybatisEnhanceInterceptor` + `EnhanceInnerInterceptor` SPI) that lets you order write-time encryption, write-time signature, post-query verification and post-query decryption in one plugin registration;
- Standalone interceptors for pagination (`PaginationInterceptor` + `Dialect`), `INSERT IGNORE` rewriting, long-SQL detection and SQL observation;
- Data-scope (row-level permission), i18n, field encryption / signature and field-fill capabilities in `mybatis-enhance-extension`, backed by JSqlParser 3.1 SQL AST tooling;
- A generic `TypeHandler` collection (arrays, JSON via Fastjson2/Jackson/Hutool, collections, Blob, Date, RSA templates) that is reusable from MyBatis-Plus projects too;
- Spring-transaction-aware service semantics (`IEnhanceService`) with save/update-then-sign workflows.

What it is not:

- Not a MyBatis-Plus alternative and not a Spring Boot starter;
- No data-masking annotations/interceptors — masking belongs to the presentation/serialization boundary (the org's `ddd4j-extension-jackson` `@Sensitive`), not to the persistence layer. The persistence layer handles reversible encryption, integrity signatures and data scope.

Typical scenarios:

| Scenario | Module / class |
| :--- | :--- |
| Encrypt-on-write, sign, verify and decrypt-on-read | `mybatis-enhance-extension` → `MybatisEnhanceInterceptor` + `DataEncryption/DataSignature/DataDecryptionInnerInterceptor` |
| Row-level data scope (annotation / script / autowire) | `mybatis-enhance-extension` → `datascope` parsers + `DefaultDataPermissionStatementInterceptor` |
| Column i18n switching | `mybatis-enhance-extension` → `i18n` handlers + `AbstractDataI18nInterceptor` |
| Pagination without a full ORM | `mybatis-enhance-core` → `PaginationInterceptor` + `MysqlDialect` |
| JSON / array / Blob columns | `mybatis-enhance-typehandler` → e.g. `JsonTypeHandler`, `ArrayStringTypeHandler` |
| Signed persistence in Spring services | `mybatis-enhance-spring` → `IEnhanceService.saveSigned / updateSignedById` |

## 2. Features & Status

| Capability | Status | Notes |
| :--- | :--- | :--- |
| Unified enhancement interceptor chain | Implemented | `MybatisEnhanceInterceptor` (Executor update/query) + `EnhanceInnerInterceptor` SPI (`before/afterUpdate`, `before/afterQuery`, `afterExecution`) |
| Pagination | Implemented | `PaginationInterceptor` + `Dialect` / `MysqlDialect` (`buildPaginationSql`, `buildCountSql`) |
| `INSERT IGNORE` rewriting | Implemented | `InsertIgnoreInterceptor` (+ global enable/reset switch) |
| Long-SQL detection | Implemented | `LongSqlInterceptor` (threshold + handler) |
| SQL observation | Implemented | `SqlObservationInterceptor` + `SqlObservationSink` / `SqlLoggingSink` |
| Field encryption / signature | Implemented | `mybatis-enhance-extension` crypto handlers + inner interceptors |
| Data scope (row-level permission) | Implemented | annotation / script / autowire parsers over JSqlParser AST |
| i18n columns | Implemented | `mybatis-enhance-extension` i18n handlers + bundles |
| Generic TypeHandlers | Implemented | 20 ready-to-use TypeHandlers + 6 abstract bases (26 classes) in `mybatis-enhance-typehandler` |
| Spring service semantics | Implemented | `IEnhanceService` / `EnhanceServiceImpl` (Spring TX) |
| Tests | Present | `MybatisEnhanceInterceptorTest`, crypto/signature integration tests, `PermissionTableVisitorTest` |

## 3. Requirements & Compatibility

| Item | Requirement |
| :--- | :--- |
| JDK | 21+ |
| Maven | 3.9.6+ (Maven Wrapper `mvnw` included) |
| MyBatis | 3.5.17 |
| JSqlParser | 3.1 (intentional compatibility baseline for the SQL AST tooling) |
| Other deps (managed in root pom) | fastjson2 2.0.62, hutool 5.8.40, jackson 2.17.2, commons-lang3, slf4j-api 2.0.18, lombok (provided) |
| Multi-module versioning | `${revision}` + flatten-maven-plugin |

Version lines (details per line in [COMPATIBILITY.md](COMPATIBILITY.md)):

| Branch | JDK | Version pattern |
| :--- | :---: | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. Architecture & Modules

```text
            +---------------------------------------------+
            |  mybatis-enhance-annotation (pure Java)     |
            |  crypto / i18n / permission annotations     |
            +---------------------+-----------------------+
                                  |
+-----------------+    +----------v-----------+    +---------------------------+
| mybatis-enhance-|    | mybatis-enhance-core |    | mybatis-enhance-extension  |
| typehandler     |    | interceptors, SPI,   |    | datascope / crypto / i18n /|
| JSON/array/Blob |    | pagination, utils    |    | fill + JSqlParser AST      |
+--------+--------+    +----------+-----------+    +-------------+-------------+
         |                        |                               |
         +------------------------+-------------------------------+
                                  |
                                  v
                           MyBatis 3.5.17
```

Five modules:

| Module | Responsibility | Standalone use |
| :--- | :--- | :--- |
| `mybatis-enhance-annotation` | Pure-Java annotations for data permission, i18n, encryption and signature | Yes, zero runtime deps |
| `mybatis-enhance-core` | Native MyBatis interceptor chain, SPI (`Dialect`, `Fillable`, ...), standalone plugins and base utils | Yes |
| `mybatis-enhance-extension` | Data scope, i18n, encryption/signature, field fill and JSqlParser SQL AST enhancement | No, needs annotation + core |
| `mybatis-enhance-typehandler` | Generic TypeHandlers: JSON, collections, Blob, Date, RSA templates, ... | Yes, also usable in MyBatis-Plus projects |
| `mybatis-enhance-spring` | Spring TX service semantics (`IEnhanceService`) with sign/verify workflows | Yes, with extension + spring-tx |

Annotation packages are organized by capability under `org.apache.ibatis.enhance.annotation.{crypto,i18n,permission}`; the crypto and i18n annotations are also reused by `mybatis-plus-enhance` (no duplicated definitions). Module dependency direction: `extension → {annotation, core}`, `spring → extension`, `typehandler → MyBatis` only.

## 5. Installation

Using only the generic TypeHandlers:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>mybatis-enhance-typehandler</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Full native-MyBatis enhancement:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>mybatis-enhance-extension</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:mybatis-enhance-extension:3.0.x.x.20260630-SNAPSHOT'
```

Only need the interceptor chain/SPI? Depend on `mybatis-enhance-core` directly. Only need annotation protocols? Depend on `mybatis-enhance-annotation`. Snapshots are served from the project's private repository (see `distributionManagement` in the pom). No Maven Central release is available yet.

## 6. Quick Start

Wire the unified enhancement chain with encryption + signature inner interceptors (pattern from the project README):

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

Lifecycle semantics: writes run `beforeUpdate` → SQL → `afterUpdate`; queries run `beforeQuery` → SQL → `afterQuery`; `afterExecution` is a bypass notification with per-interceptor exception isolation. Inner interceptors execute strictly in registration order — since signature fields depend on ciphertext, register encryption before signature.

## 7. Configuration

No application-property prefix. Behavior is configured programmatically:

- inner-interceptor order in `MybatisEnhanceInterceptor.addInterceptor(...)`;
- `LongSqlInterceptor(longSqlThreshold, longSqlHandler)` thresholds;
- `InsertIgnoreInterceptor.enable()/reset()` global switch;
- `Dialect` selection for `PaginationInterceptor` (e.g. `new PaginationInterceptor(new MysqlDialect())`);
- crypto key material through `CryptoKeyMaterial` / `StaticCryptoKeyProvider` and handlers (`DataEncryptionHandler`, `DataSignatureHandler`, `EncryptedFieldHandler`);
- tenant/i18n contexts via `TenantContext` and `DataInputProvider`.

## 8. Core Usage / API

### 8.1 Pagination interceptor

```java
import org.apache.ibatis.enhance.plugins.PaginationInterceptor;
import org.apache.ibatis.enhance.spi.MysqlDialect;

PaginationInterceptor pagination = new PaginationInterceptor(new MysqlDialect());
configuration.addInterceptor(pagination);
// Dialect contract: String buildPaginationSql(originalSql, offset, size);
//                  String buildCountSql(originalSql);
```

### 8.2 Signed persistence via Spring service

```java
import org.apache.ibatis.enhance.service.IEnhanceService;

IEnhanceService<User> service = ...;

service.saveSigned(user);              // insert + sign by id (Spring @Transactional)
service.updateSignedById(user);        // update + re-sign
service.saveBatchSigned(userList);     // batch insert + sign (default batch size 1000)
```

`EnhanceMapper<T>` provides `insert / updateById / selectById / selectBatchIds / selectList` plus `@IgnoreEncrypted` variants (`selectIgnoreDecryptById`, `selectIgnoreDecryptBatchIds`) that skip decryption on read.

### 8.3 Data scope with JSqlParser

`mybatis-enhance-extension` keeps JSqlParser-AST-based data-scope rewriting: `QueryTablesNamesFinder` extracts tables; `SelectAutowirePermissionParser`, `SelectAnnotationPermission(s)Parser` and `SelectAnnotationSpecialPermission(s)Parser` apply autowired / annotated / special permission strategies; `SqlParserTool` is the general AST parse/rewrite entry. The visitors reuse JSqlParser's official `TablesNamesFinder` traversal so only the permission-replacement policy is maintained here.

### 8.4 Masking boundary (explicitly out of scope)

Data masking is a serialization-boundary concern. This project does not modify write parameters, query results or cached objects in place: the persistence layer handles reversible encryption, integrity signature and data scope; the presentation layer generates masked views (the org's `ddd4j-extension-jackson` provides `@Sensitive` + `SensitiveStrategy` + `SensitiveJsonSerializer`).

## 9. Testing & Build

```bash
./mvnw clean verify
```

Multi-module versions are driven by `${revision}`; `flatten-maven-plugin` generates the consumable POMs at build time. Release:

```bash
./mvnw -Prelease clean deploy -Drevision=<release-version>
```

Quality gates:

- Test suites: `MybatisEnhanceInterceptorTest` (core), crypto/signature integration tests (`CryptoSignatureIntegrationTest`, `CryptoHandlersTest`, `CryptoMapper` + `CryptoMapper.xml`) and `PermissionTableVisitorTest` (extension);
- JaCoCo coverage reporting plus a line-coverage check rule with a 90% minimum target (`haltOnFailure=false`);
- Source and Javadoc jars attached at package time; Maven Enforcer rules guard the build environment.

## 10. Versioning & Branches

Three parallel version lines, each bound to a JDK baseline:

| Branch | JDK | Version pattern | Maintenance |
| :--- | :---: | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | Current development line |
| `feature/2.0.x` | 17 | `2.0.x.*` | Maintained in parallel |
| `feature/3.0.x` | 21 | `3.0.x.*` | Maintained in parallel |

Cross-line sync targets 1:1 capability parity, but dependency versions, compile settings, JSqlParser API adaptations and Java syntax must match each line's JDK baseline — build configs are not copied mechanically (see [COMPATIBILITY.md](COMPATIBILITY.md)). The current branch snapshot is `3.0.x.x.20260630-SNAPSHOT`; releases are cut via GitHub Releases, Maven Central publication is planned but not yet done.

## 11. Contributing & License

Contributions are welcome — open an issue or pull request on GitHub. All source files are licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
