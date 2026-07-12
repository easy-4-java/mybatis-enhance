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

    @Override
    protected Double parseElement(String element) {
        return Double.valueOf(element);
    }

    @Override
    protected Double[] toArray(List<Double> list) {
        return list.toArray(new Double[0]);
    }

    @Override
    protected Double[] newArray(int length) {
        return new Double[length];
    }
}
