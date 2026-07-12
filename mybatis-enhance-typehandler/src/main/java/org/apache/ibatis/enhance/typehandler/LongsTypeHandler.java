package org.apache.ibatis.enhance.typehandler;

import java.util.List;

/**
 * 类型转换：varchar &lt;-&gt; Long[]，使用英文逗号 {@code ,} 分割。
 *
 * <p>基于 {@link AbstractCommaArrayTypeHandler}，序列化与反序列化的样板由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
public class LongsTypeHandler extends AbstractCommaArrayTypeHandler<Long> {

    @Override
    protected Long parseElement(String element) {
        return Long.valueOf(element);
    }

    @Override
    protected Long[] toArray(List<Long> list) {
        return list.toArray(new Long[0]);
    }

    @Override
    protected Long[] newArray(int length) {
        return new Long[length];
    }
}
