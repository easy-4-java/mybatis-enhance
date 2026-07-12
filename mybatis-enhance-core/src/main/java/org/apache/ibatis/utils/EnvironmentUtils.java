/*
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
package org.apache.ibatis.utils;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

/**
 * {@code EnvironmentUtils} 工具类。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class EnvironmentUtils {

    /**
     * 获取 {@code transactionFactoryFromEnvironment}。
     *
     * @param configuration MyBatis 配置
     * @return 对应的属性值
     */
    public static TransactionFactory getTransactionFactoryFromEnvironment(Configuration configuration) {
        final Environment environment = configuration.getEnvironment();
        return getTransactionFactoryFromEnvironment(environment);
    }

    /**
     * 获取 {@code transactionFactoryFromEnvironment}。
     *
     * @param environment MyBatis 运行环境
     * @return 对应的属性值
     */
    public static TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
        if (environment == null || environment.getTransactionFactory() == null) {
            return new ManagedTransactionFactory();
        }
        return environment.getTransactionFactory();
    }

}
