package org.apache.ibatis.enhance.typehandler;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TypeHandlerContractTest {

    // ===== 原有：Date / Blob / RSA =====

    @Test
    public void dateHandlerShouldReturnNullForDatabaseNull() throws Exception {
        DateTypeHandler handler = new DateTypeHandler();
        ResultSet resultSet = resultSet(null);
        Assert.assertNull(handler.getNullableResult(resultSet, 1));
        Assert.assertNull(handler.getNullableResult(resultSet, "created_at"));
    }

    @Test
    public void blobHandlerShouldDecodeBothResultSetOverloads() throws Exception {
        BlobStringTypeHandler handler = new BlobStringTypeHandler();
        SerialBlob blob = new SerialBlob("中文内容".getBytes(StandardCharsets.UTF_8));
        ResultSet resultSet = resultSet(blob);
        Assert.assertEquals("中文内容", handler.getNullableResult(resultSet, 1));
        Assert.assertEquals("中文内容", handler.getNullableResult(resultSet, "payload"));
    }

    @Test
    public void rsaTemplateShouldDelegateEncryptionAndDecryption() throws Exception {
        RSAStringTypeHandler handler = new RSAStringTypeHandler() {
            @Override
            protected String encrypt(String plainText) {
                return "cipher:" + plainText;
            }

            @Override
            protected String decrypt(String cipherText) {
                return cipherText.substring("cipher:".length());
            }
        };
        AtomicReference<String> storedValue = new AtomicReference<>();
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setString".equals(method.getName())) {
                        storedValue.set((String) args[1]);
                    }
                    return defaultValue(method.getReturnType());
                });

        handler.setNonNullParameter(statement, 1, "secret", JdbcType.VARCHAR);
        Assert.assertEquals("cipher:secret", storedValue.get());
        Assert.assertEquals("secret", handler.getNullableResult(resultSet("cipher:secret"), 1));
    }

    // ===== 新增：逗号数组基类 round-trip =====

    @Test
    public void stringsTypeHandlerShouldRoundTripCommaSeparated() throws Exception {
        StringsTypeHandler handler = new StringsTypeHandler();
        String[] input = {"a", "b", "c"};
        String stored = handler.convert(input);
        Assert.assertEquals("a,b,c", stored);
        Assert.assertArrayEquals(input, handler.parse(stored));
    }

    @Test
    public void longsTypeHandlerShouldRoundTripCommaSeparated() throws Exception {
        LongsTypeHandler handler = new LongsTypeHandler();
        Long[] input = {1L, 2L, 3L};
        String stored = handler.convert(input);
        Assert.assertEquals("1,2,3", stored);
        Assert.assertArrayEquals(input, handler.parse(stored));
    }

    @Test
    public void integersTypeHandlerShouldRoundTripSingleElement() throws Exception {
        IntegersTypeHandler handler = new IntegersTypeHandler();
        Integer[] input = {42};
        String stored = handler.convert(input);
        Assert.assertEquals("42", stored);
        Assert.assertArrayEquals(input, handler.parse(stored));
    }

    @Test
    public void doublesTypeHandlerShouldReturnNullForEmptyArray() throws Exception {
        DoublesTypeHandler handler = new DoublesTypeHandler();
        Assert.assertNull(handler.convert(new Double[]{}));
    }

    @Test
    public void commaArrayHandlersShouldReturnEmptyArrayForNullOrEmptyString() throws Exception {
        LongsTypeHandler handler = new LongsTypeHandler();
        Assert.assertEquals(0, handler.parse(null).length);
        Assert.assertEquals(0, handler.parse("").length);
    }

    // ===== 新增：FastJSON 基类 round-trip =====

    @Test
    public void fastJsonTypeHandlerShouldRoundTripObject() throws Exception {
        FastJsonTypeHandler handler = new FastJsonTypeHandler();
        JSONObject obj = new JSONObject();
        obj.put("name", "张三");
        String stored = handler.convert(obj);
        JSONObject parsed = handler.parse(stored);
        Assert.assertEquals("张三", parsed.getString("name"));
    }

    @Test
    public void fastJsonArrayTypeHandlerShouldRoundTripArray() throws Exception {
        FastJsonArrayTypeHandler handler = new FastJsonArrayTypeHandler();
        JSONArray arr = new JSONArray();
        arr.add(1);
        arr.add(2);
        String stored = handler.convert(arr);
        JSONArray parsed = handler.parse(stored);
        Assert.assertEquals(2, parsed.size());
    }

    // ===== 新增：Hutool JSON 基类 round-trip =====

    @Test
    public void jsonTypeHandlerShouldRoundTripObject() throws Exception {
        JsonTypeHandler handler = new JsonTypeHandler();
        cn.hutool.json.JSONObject obj = new cn.hutool.json.JSONObject();
        obj.put("key", "value");
        String stored = handler.convert(obj);
        cn.hutool.json.JSONObject parsed = handler.parse(stored);
        Assert.assertEquals("value", parsed.getStr("key"));
    }

    @Test
    public void jsonArrayTypeHandlerShouldRoundTripArray() throws Exception {
        JsonArrayTypeHandler handler = new JsonArrayTypeHandler();
        cn.hutool.json.JSONArray arr = new cn.hutool.json.JSONArray(Arrays.asList(1, 2, 3));
        String stored = handler.convert(arr);
        cn.hutool.json.JSONArray parsed = handler.parse(stored);
        Assert.assertEquals(3, parsed.size());
    }

    // ===== 新增：List/Set 集合 + 空值 bug 回归 =====

    @Test
    public void listStringTypeHandlerShouldRoundTrip() throws Exception {
        ListStringTypeHandler handler = new ListStringTypeHandler();
        List<String> input = Arrays.asList("a", "b");
        String stored = handler.convert(input);
        Assert.assertEquals("[\"a\",\"b\"]", stored);
        Assert.assertEquals(input, handler.parse(stored));
    }

    @Test
    public void setStringTypeHandlerShouldRoundTripAndWrapAsHashSet() throws Exception {
        SetStringTypeHandler handler = new SetStringTypeHandler();
        Set<String> input = new HashSet<>(Arrays.asList("x", "y"));
        String stored = handler.convert(input);
        Set<String> parsed = handler.parse(stored);
        Assert.assertEquals(input, parsed);
        // 验证 wrapCollection 真的包成了 Set
        Assert.assertTrue(parsed instanceof Set);
    }

    @Test
    public void collectionHandlersShouldReturnNullForBlankString() throws Exception {
        // 回归测试：修复旧版 ListString/SetString 空值判断不一致的 bug
        ListStringTypeHandler listHandler = new ListStringTypeHandler();
        Assert.assertNull(listHandler.parse(null));
        Assert.assertNull(listHandler.parse(""));

        SetStringTypeHandler setHandler = new SetStringTypeHandler();
        Assert.assertNull(setHandler.parse(null));
        Assert.assertNull(setHandler.parse(""));
    }

    @Test
    public void collectionHandlersShouldReadNullViaJdbc() throws Exception {
        // 通过 JDBC 路径验证空值处理（BaseTypeHandler 的 isBlank 判空）
        ListStringTypeHandler handler = new ListStringTypeHandler();
        ResultSet rs = resultSet(null);
        Assert.assertNull(handler.getNullableResult(rs, 1));
        Assert.assertNull(handler.getNullableResult(rs, "col"));

        ResultSet blankRs = resultSet("");
        Assert.assertNull(handler.getNullableResult(blankRs, 1));
    }

    // ===== 辅助方法 =====

    private ResultSet resultSet(Object value) {
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
                    if ("getDate".equals(method.getName()) || "getBlob".equals(method.getName())
                            || "getString".equals(method.getName())) {
                        return value;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        return null;
    }
}
