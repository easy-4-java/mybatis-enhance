package org.apache.ibatis.enhance.typehandler;

import java.util.List;

/**
 * 类型转换：varchar &lt;-&gt; String[]，使用英文逗号 {@code ,} 分割。
 *
 * <p>基于 {@link AbstractCommaArrayTypeHandler}，序列化与反序列化的样板由基类统一处理。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
public class StringsTypeHandler extends AbstractCommaArrayTypeHandler<String> {

    @Override
    protected String parseElement(String element) {
        return element;
    }

    @Override
    protected String[] toArray(List<String> list) {
        return list.toArray(new String[0]);
    }

    @Override
    protected String[] newArray(int length) {
        return new String[length];
    }
}
