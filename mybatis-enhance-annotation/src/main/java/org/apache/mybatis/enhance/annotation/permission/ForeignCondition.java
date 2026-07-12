/**
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.mybatis.enhance.annotation.permission;

import java.util.*;

/**
 * 关联表数据权限条件。
 *
 * <p>用于描述主表字段与权限关联表字段之间的比较方式。</p>
 */
public enum ForeignCondition {

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
	 * 创建关联表权限条件。
	 *
	 * @param operator SQL 条件格式模板
	 * @param placeholder 条件说明
	 */
	ForeignCondition(String operator, String placeholder) {
        this.operator = operator;
        this.placeholder = placeholder;
    }

    /**
     * 根据 SQL 模板解析关联条件。
     *
     * @param operator SQL 条件模板
     * @return 匹配的关联条件
     * @throws NoSuchElementException 不支持指定模板时抛出
     */
    public static ForeignCondition fromString(String operator){
        for (ForeignCondition condition : ForeignCondition.values()) {
            if(condition.operator.equals(operator.toUpperCase()) ){
                return condition;
            }
        }
        throw new NoSuchElementException("Filter operator " + operator + " is not supported!");
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
	 * 获取全部关联条件的配置项列表。
	 *
	 * @return 关联条件配置列表
	 */
	public static List<Map<String, String>> toList() {
		List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
		for (ForeignCondition condition : ForeignCondition.values()) {
			mapList.add(condition.toMap());
		}
		return mapList;
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
