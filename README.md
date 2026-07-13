# mybatis-enhance

`mybatis-enhance` 是面向原生 MyBatis 的增强组件。项目只依赖 MyBatis 核心 API，不依赖 Spring、MyBatis-Spring 或
MyBatis-Plus；需要 MyBatis-Plus 专属能力时，应使用独立项目 `mybatis-plus-enhance`。

本项目保留四个职责清晰的模块：

| 模块                            | 职责                                          | 可独立使用                  |
|-------------------------------|---------------------------------------------|------------------------|
| `mybatis-enhance-annotation`  | 数据权限、国际化、加解密和签名等纯 Java 注解                   | 是，零运行时依赖               |
| `mybatis-enhance-core`        | 原生 MyBatis 拦截器链、SPI、通用插件与基础工具               | 是                      |
| `mybatis-enhance-extension`   | 数据权限、国际化、加解密、签名、字段填充及 JSqlParser SQL AST 增强 | 否，依赖 annotation 与 core |
| `mybatis-enhance-typehandler` | JSON、集合、日期、Blob、RSA 模板等通用 TypeHandler       | 是，可用于 MyBatis-Plus 项目  |

公共注解按能力域组织，避免所有协议堆积在同一个根包：

```text
org.apache.ibatis.enhance.annotation.crypto
org.apache.ibatis.enhance.annotation.i18n
org.apache.ibatis.enhance.annotation.permission
```

其中 crypto 与 i18n 注解可被 `mybatis-plus-enhance` 直接复用；MyBatis-Plus 不再维护重复定义。

模块依赖方向如下：

```text
mybatis-enhance-extension --> mybatis-enhance-annotation
             |
             +-------------> mybatis-enhance-core --> MyBatis

mybatis-enhance-typehandler ------------------------> MyBatis
```

## 环境与版本

当前 `1.0.x` 分支以 JDK 8 为基线：

- JDK 8
- Maven 3.9.6+
- MyBatis 3.5.17
- JSqlParser 3.1

JSqlParser 3.1 是有意保留的兼容基线。`net.sf.jsqlparser.util` 下的数据权限 Visitor 和 SQL AST
工具属于本项目的增强能力，不是需要清除的第三方包侵入。其他版本线见 [COMPATIBILITY.md](COMPATIBILITY.md)。

## Maven 依赖

仅使用通用 TypeHandler：

```xml

<dependency>
    <groupId>io.github.hiwepy</groupId>
    <artifactId>mybatis-enhance-typehandler</artifactId>
    <version>${mybatis-enhance.version}</version>
</dependency>
```

使用完整原生 MyBatis 增强能力：

```xml

<dependency>
    <groupId>io.github.hiwepy</groupId>
    <artifactId>mybatis-enhance-extension</artifactId>
    <version>${mybatis-enhance.version}</version>
</dependency>
```

只需要拦截器链或 SPI 时，可以直接依赖 `mybatis-enhance-core`。只需要注解协议时，可以直接依赖 `mybatis-enhance-annotation`。

## 原生 MyBatis 增强链

`MybatisEnhanceInterceptor` 是统一外层插件，内部的 `EnhanceInterceptor` 按注册顺序执行。这样可以用一个 MyBatis
插件明确控制写入前加密、写入前签名、查询后验签和查询后解密等生命周期。

```java
EncryptedFieldHandler encryptedFieldHandler = createEncryptedFieldHandler();
DataSignatureHandler signatureHandler = createSignatureHandler();

MybatisEnhanceInterceptor enhanceInterceptor = new MybatisEnhanceInterceptor();
enhanceInterceptor.

addInterceptor(new DataEncryptionInterceptor(encryptedFieldHandler));
        enhanceInterceptor.

addInterceptor(new DataSignatureInterceptor(signatureHandler));
        enhanceInterceptor.

addInterceptor(new DataDecryptionInterceptor(encryptedFieldHandler));

Configuration configuration = new Configuration();
configuration.

addInterceptor(enhanceInterceptor);
```

写操作调用 `beforeUpdate` 后执行 SQL，再调用 `afterUpdate`；查询操作调用 `beforeQuery` 后执行 SQL，再调用 `afterQuery`
。同一生命周期内严格遵循注册顺序，因此签名字段依赖密文时，应先注册加密增强器，再注册签名增强器。

## JSqlParser 数据权限增强

`mybatis-enhance-extension` 保留了基于 JSqlParser AST 的数据权限能力，能够遍历主查询、JOIN
和嵌套子查询中的表，并按注解、自动装配或特殊权限策略替换表表达式。公共入口包括：

- `QueryTablesNamesFinder`：提取查询涉及的表；
- `SelectAutowirePermissionParser`：应用自动装配的数据权限；
- `SelectAnnotationPermissionParser` / `SelectAnnotationPermissionsParser`：应用单个或多个注解权限；
- `SelectAnnotationSpecialPermissionParser` / `SelectAnnotationSpecialPermissionsParser`：应用特殊权限；
- `SqlParserTool`：提供基于 AST 的 SQL 解析与改写工具。

这些类复用 JSqlParser 官方 `TablesNamesFinder` 完成统一遍历，项目代码只维护权限替换策略，避免多份 Visitor 实现产生行为漂移。

## TypeHandler 复用边界

`mybatis-enhance-typehandler` 只有 MyBatis 与序列化工具依赖，不包含 Spring 和 MyBatis-Plus API。因此它既可用于原生
MyBatis，也可作为基础能力被 MyBatis-Plus 项目直接依赖。模块内置 Maven Enforcer 规则，防止后续误引入框架集成依赖。

## 脱敏能力边界

数据脱敏属于接口输出或序列化边界，不应在 MyBatis 写入参数、查询结果或缓存对象上原地修改。因此本项目不提供脱敏注解和脱敏拦截器：

- 持久化层负责可逆加解密、完整性签名和数据权限；
- 表现层负责按调用场景生成脱敏视图；
- ddd4j 项目统一使用 `ddd4j-extension-jackson` 的 `@Sensitive`、`SensitiveStrategy` 和 `SensitiveJsonSerializer`；
- 非 Jackson 项目应在其 DTO 映射或序列化适配器中实现同等输出策略。

## 构建与验证

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 1.8) mvn clean verify
```

多模块版本统一由 `${revision}` 管理，`flatten-maven-plugin` 会在构建时生成可发布、可消费的 POM。发布时使用：

```bash
mvn -Prelease clean deploy -Drevision=<release-version>
```
