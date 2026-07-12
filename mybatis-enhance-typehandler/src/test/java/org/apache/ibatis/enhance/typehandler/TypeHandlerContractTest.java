package org.apache.ibatis.enhance.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicReference;

public class TypeHandlerContractTest {

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
