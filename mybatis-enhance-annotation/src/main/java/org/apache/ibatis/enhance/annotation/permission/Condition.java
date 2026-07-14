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
 * 主表数据权限条件。
 *
 * <p>每个枚举值同时保存 SQL 格式模板和面向配置界面的中文说明。SQL 模板中的占位符由
 * 权限解析器填入表别名、列名和权限参数。</p>
 */
public enum Condition {

    /***
     * 大于
     */
    GT(" %s.%s > %s ", "[数据字段]大于[数据项]"),
    /***
     * 大于或等于
     */
    GTE(" %s.%s >= %s ", "[数据字段]大于或等于[数据项]"),
    /***
     * 小于
     */
    LT(" %s.%s < %s ", "[数据字段]小于[数据项]"),
    /***
     * 小于或等于
     */
    LTE(" %s.%s <= %s ", "[数据字段]小于或等于[数据项]"),
    /***
     * 等于
     */
    EQ(" %s.%s = %s ", "[数据字段]等于[数据项]"),
    /***
     * 不等于
     */
    NE(" %s.%s != %s ", "[数据字段]不等于[数据项]"),
    /***
     * 在指定范围 in ()
     */
    IN(" %s.%s IN (%s) ", "[数据字段]在[数据项]范围内"),
    /***
     * % 两边 %
     */
    LIKE(" %s.%s LIKE CONCAT('%%', %s ,'%%') ", "[数据字段]包含指定[数据项]"),
    /***
     * % 左
     */
    LIKE_LEFT(" %s.%s LIKE CONCAT('%%', %s) ", "[数据字段]以[数据项]开始"),
    /***
     * 右 %
     */
    LIKE_RIGHT(" %s.%s LIKE CONCAT(%s,'%%') ", "[数据字段]以[数据项]结束"),

    /** 存在满足条件的关联记录。 */
    EXISTS(" EXISTS ( %s ) ", "[数据字段]不在指定的[数据项]范围内"),
    /** 不存在满足条件的关联记录。 */
    NOT_EXISTS(" NOT EXISTS ( %s ) ", "[数据字段]在指定的[数据项]范围内"),

    /** 按位与结果大于零。 */
    BITAND_GT(" bitand(%s, to_number(%s.%s)) > 0", "[数据字段]与[数据项]按位运行大于0：bitand(数据项, 数据字段) > 0"),
    /** 按位与结果大于或等于零。 */
    BITAND_GTE(" bitand(%s, to_number(%s.%s)) >= 0", "[数据字段]与[数据项]按位运行大于或等于0：bitand(数据项, 数据字段) >= 0"),
    /** 按位与结果小于零。 */
    BITAND_LT(" bitand(%s, to_number(%s.%s)) < 0", "[数据字段]与[数据项]按位运行小于0：bitand(数据项, 数据字段) < 0"),
    /** 按位与结果小于或等于零。 */
    BITAND_LTE(" bitand(%s, to_number(%s.%s)) <= 0", "[数据字段]与[数据项]按位运行小于或等于0：bitand(数据项, 数据字段) <= 0"),
    /** 按位与结果等于零。 */
    BITAND_EQ(" bitand(%s, to_number(%s.%s)) = 0", "[数据字段]与[数据项]按位运行等于0：bitand(数据项, 数据字段) => 0");

    /** SQL 条件格式模板。 */
    private final String operator;
    /** 面向配置界面的条件说明。 */
    private final String placeholder;

    /**
     * 创建权限条件。
     *
     * @param operator SQL 条件格式模板
     * @param placeholder 条件说明
     */
    Condition(String operator, String placeholder) {
        this.operator = operator;
        this.placeholder = placeholder;
    }

    /**
     * 根据 SQL 模板解析权限条件。
     *
     * @param operator SQL 条件模板
     * @return 匹配的权限条件
     * @throws NoSuchElementException 不支持指定模板时抛出
     */
    public static Condition fromString(String operator) {
        for (Condition condition : Condition.values()) {
            if (condition.operator.equals(operator.toUpperCase())) {
                return condition;
            }
        }
        throw new NoSuchElementException("Filter operator " + operator + " is not supported!");
    }

    /**
     * 获取全部条件的配置项列表。
     *
     * @return 条件配置列表
     */
    public static List<Map<String, String>> toList() {
        List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
        for (Condition condition : Condition.values()) {
            mapList.add(condition.toMap());
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
     * 获取 SQL 条件格式模板。
     *
     * @return SQL 模板
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 获取条件说明。
     *
     * @return 中文说明
     */
    public String getPlaceholder() {
        return placeholder;
    }

}
