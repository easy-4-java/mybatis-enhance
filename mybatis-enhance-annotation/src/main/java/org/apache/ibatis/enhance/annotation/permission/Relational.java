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
package org.apache.ibatis.enhance.annotation.permission;

import java.util.*;

/**
 * 多个数据权限条件之间的逻辑关系。
 */
public enum Relational {

    /** 所有条件均需成立。 */
    AND(" AND ", "且"),
    /** 任一条件成立即可。 */
    OR(" OR ", "或");

    /** SQL 逻辑运算符。 */
    private final String operator;
    /** 面向配置界面的逻辑说明。 */
    private final String placeholder;

    /**
     * 创建逻辑关系。
     *
     * @param operator SQL 逻辑运算符
     * @param placeholder 逻辑说明
     */
    Relational(String operator, String placeholder) {
        this.operator = operator;
        this.placeholder = placeholder;
    }

    /**
     * 忽略大小写和首尾空白解析逻辑关系。
     *
     * @param operator SQL 逻辑运算符
     * @return 匹配的逻辑关系
     * @throws NullPointerException 运算符为 {@code null} 时抛出
     * @throws IllegalArgumentException 不支持指定运算符时抛出
     */
    public static Relational fromString(String operator) {
        String normalized = Objects.requireNonNull(operator, "Operator must not be null").trim();
        for (Relational relational : Relational.values()) {
            if (relational.operator.trim().equalsIgnoreCase(normalized)) {
                return relational;
            }
        }
        throw new IllegalArgumentException("Operator " + operator + " is not supported");
    }

    /**
     * 获取全部逻辑关系的配置项列表。
     *
     * @return 逻辑关系配置列表
     */
    public static List<Map<String, String>> toList() {
        List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
        for (Relational relational : Relational.values()) {
            mapList.add(relational.toMap());
        }
        return mapList;
    }

    /**
     * 转换为配置项键值对。
     *
     * @return 包含枚举名称和中文说明的映射
     */
    public Map<String, String> toMap() {
        Map<String, String> driverMap = new HashMap<String, String>();
        driverMap.put("key", this.name());
        driverMap.put("value", this.getPlaceholder());
        return driverMap;
    }

    /**
     * 获取 SQL 逻辑运算符。
     *
     * @return SQL 运算符
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 获取逻辑关系说明。
     *
     * @return 中文说明
     */
    public String getPlaceholder() {
        return placeholder;
    }

}
