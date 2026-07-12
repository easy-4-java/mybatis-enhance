package org.apache.ibatis.enhance.typehandler;

import java.util.List;

/**
 * 类型转换：varchar &lt;-&gt; Double[]，使用英文逗号 {@code ,} 分割。
 *
 * <p>基于 {@link AbstractCommaArrayTypeHandler}，序列化与反序列化的样板由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
public class DoublesTypeHandler extends AbstractCommaArrayTypeHandler<Double> {

    /**
     * 解析 {@code parseElement} 定义的框架操作。
     *
     * @param element 调用参数 {@code element}
     * @return 处理结果
     */
    @Override
    protected Double parseElement(String element) {
        return Double.valueOf(element);
    }

    /**
     * 转换 {@code toArray} 定义的框架操作。
     *
     * @param list 调用参数 {@code list}
     * @return 处理结果
     */
    @Override
    protected Double[] toArray(List<Double> list) {
        return list.toArray(new Double[0]);
    }

    /**
     * 创建 {@code newArray} 定义的框架操作。
     *
     * @param length 调用参数 {@code length}
     * @return 处理结果
     */
    @Override
    protected Double[] newArray(int length) {
        return new Double[length];
    }
}
