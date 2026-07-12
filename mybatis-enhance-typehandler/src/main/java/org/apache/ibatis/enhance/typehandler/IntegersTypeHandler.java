package org.apache.ibatis.enhance.typehandler;

import java.util.List;

/**
 * 类型转换：varchar &lt;-&gt; Integer[]，使用英文逗号 {@code ,} 分割。
 *
 * <p>基于 {@link AbstractCommaArrayTypeHandler}，序列化与反序列化的样板由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
public class IntegersTypeHandler extends AbstractCommaArrayTypeHandler<Integer> {

    @Override
    protected Integer parseElement(String element) {
        return Integer.valueOf(element);
    }

    @Override
    protected Integer[] toArray(List<Integer> list) {
        return list.toArray(new Integer[0]);
    }

    @Override
    protected Integer[] newArray(int length) {
        return new Integer[length];
    }
}
