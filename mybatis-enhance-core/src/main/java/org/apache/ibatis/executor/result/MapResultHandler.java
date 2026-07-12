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
package org.apache.ibatis.executor.result;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code MapResultHandler} 处理器。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MapResultHandler implements ResultHandler<String> {

    private final List<Map<String, String>> list;

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     */
    public MapResultHandler() {
        list = new ArrayList<Map<String, String>>();
    }

    /**
     * 创建实例并初始化运行所需的上下文。
     *
     * @param objectFactory 对象工厂
     */
    public MapResultHandler(ObjectFactory objectFactory) {
        list = objectFactory.create(List.class);
    }

    /**
     * 处理 {@code handleResult} 定义的框架操作。
     *
     * @param context 调用参数 {@code context}
     */
    public void handleResult(ResultContext<? extends String> context) {
        Object object = context.getResultObject();
        if (object instanceof Map) {
            list.add((Map) object);
        } else {
            try {
                list.add(BeanUtils.describe(object));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取 {@code resultList}。
     *
     * @return 对应的属性值
     */
    public List<Map<String, String>> getResultList() {
        return list;
    }


}
