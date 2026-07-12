/***
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
package org.apache.mybatis.enhance.i18n.i18n.bundle;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * {@code EmptyResourceBundle} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class EmptyResourceBundle extends ResourceBundle {

    /**
     * 获取 {@code keys}。
     *
     * @return 对应的属性值
     */
    @Override
    public Enumeration<String> getKeys() {
        return null; // dummy
    }

    /**
     * 处理 {@code handleGetObject} 定义的框架操作。
     *
     * @param key 键
     * @return 处理结果
     */
    @Override
    protected Object handleGetObject(String key) {
        return null; // dummy
    }

}
