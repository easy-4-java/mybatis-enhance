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
package org.apache.ibatis.enhance.i18n.i18n.bundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * {@code MultipleResourceBundle} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class MultipleResourceBundle extends ResourceBundle {

    protected static Logger LOG = LoggerFactory.getLogger(MultipleResourceBundle.class);
    protected ResourceBundle[] bundles;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     */
    public MultipleResourceBundle() {
    }

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param bundles 调用参数 {@code bundles}
     */
    public MultipleResourceBundle(ResourceBundle... bundles) {
        this.bundles = bundles;
    }

    /**
     * 处理 {@code handleGetObject} 定义的框架操作。
     *
     * @param key 键
     * @return 处理结果
     */
    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException("key is null ");
        }
        for (ResourceBundle bundle : bundles) {
            try {
                Object value = bundle.getObject(key);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                // ingrone e
                LOG.warn(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取 {@code keys}。
     *
     * @return 对应的属性值
     */
    @Override
    public Enumeration<String> getKeys() {
        return new ResourceBundleEnumeration(this.parent, this.bundles);
    }

};
