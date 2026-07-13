/**
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/parser/def/TablePermissionAutowireParser.java
package org.apache.ibatis.enhance.datascope.parser.def;
========
package org.apache.ibatis.enhance.dbperms.parser.def;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/parser/def/TablePermissionAutowireParser.java

import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectAutowirePermissionParser;
import org.apache.ibatis.binding.MetaStatementHandler;
import org.apache.ibatis.exception.MybatisException;
<<<<<<<< HEAD:mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/datascope/parser/def/TablePermissionAutowireParser.java
import org.apache.ibatis.enhance.datascope.parser.ITablePermissionAutowireHandler;
import org.apache.ibatis.enhance.datascope.parser.ITablePermissionParser;
========
import org.apache.ibatis.enhance.dbperms.parser.ITablePermissionAutowireHandler;
import org.apache.ibatis.enhance.dbperms.parser.ITablePermissionParser;
>>>>>>>> 8e73aaa (fix(rename): PR-A1 遗留清理 — 补齐剩余 52 个文件的 mybatis → ibatis 路径迁移):mybatis-enhance-extension/src/main/java/org/apache/ibatis/enhance/dbperms/parser/def/TablePermissionAutowireParser.java

/**
 * {@code TablePermissionAutowireParser} 解析器。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@Data
@Accessors(chain = true)
public class TablePermissionAutowireParser implements ITablePermissionParser {

    private ITablePermissionAutowireHandler tablePermissionHandler;

    private volatile boolean initialized = false;

    /**
     * Initialize the object.
     */
    public void init() {
        if (!this.initialized) {
            synchronized (this) {
                if (!this.initialized) {
                    internalInit();
                    this.initialized = true;
                }
            }
        }
    }

    /**
     * Internal initialization of the object.
     */
    protected void internalInit() {
    }

    ;

    /**
     * 解析 {@code parser} 定义的框架操作。
     *
     * @param metaHandler 调用参数 {@code metaHandler}
     * @param sql SQL 文本
     * @return 处理结果
     */
    public String parser(MetaStatementHandler metaHandler, String sql) {
        if (!this.doFilter(metaHandler, sql)) {
            return sql;
        }
        this.init();
        String parsedSQL = sql;
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (null != statement && statement instanceof Select) {
                Select select = (Select) statement;
                // 动态修改SQL
                select.accept(new SelectAutowirePermissionParser(this.getTablePermissionHandler(), metaHandler));
                // 获取处理后的SQL
                parsedSQL = select.getSelectBody().toString();
            }
        } catch (JSQLParserException e) {
            throw new MybatisException(String.format("Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e, sql), e);
        }
        return parsedSQL;
    }

}
