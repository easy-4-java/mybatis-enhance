package org.apache.mybatis.enhance.crypto.handler;

import org.apache.mybatis.enhance.crypto.annotation.EncryptedField;
import org.apache.mybatis.enhance.crypto.annotation.EncryptedTable;
import org.apache.mybatis.enhance.crypto.annotation.TableSignature;
import org.apache.mybatis.enhance.crypto.annotation.TableSignatureField;
import org.junit.Assert;
import org.junit.Test;

public class CryptoHandlersTest {

    @Test
    public void shouldEncryptAndDecryptAnnotatedFields() {
        SampleEntity entity = new SampleEntity();
        entity.mobile = "13800138000";
        DefaultDataEncryptionHandler handler = new DefaultDataEncryptionHandler(new StubFieldHandler());

        Assert.assertTrue(handler.doEntityEncrypt(entity));
        Assert.assertEquals("ENC(13800138000)", entity.mobile);

        handler.doRawObjectDecrypt(entity, SampleEntity.class);
        Assert.assertEquals("13800138000", entity.mobile);
    }

    @Test
    public void shouldSignAndVerifyEntity() {
        SignedEntity entity = new SignedEntity();
        entity.orderNo = "ORDER-1";
        entity.amount = 100;
        DefaultDataSignatureHandler handler = new DefaultDataSignatureHandler(new StubFieldHandler());

        Assert.assertTrue(handler.doEntitySignature(entity));
        Assert.assertEquals("HMAC(ORDER-1|100)", entity.signature);
        handler.doSignatureVerification(entity, SignedEntity.class);

        entity.amount = 101;
        try {
            handler.doSignatureVerification(entity, SignedEntity.class);
            Assert.fail("Expected signature mismatch");
        } catch (IllegalStateException expected) {
            Assert.assertTrue(expected.getMessage().contains("signature mismatch"));
        }
    }

    @EncryptedTable
    private static class SampleEntity {

        @EncryptedField
        private String mobile;
    }

    @TableSignature
    private static class SignedEntity {

        @TableSignatureField(order = 1)
        private String orderNo;

        @TableSignatureField(order = 2)
        private int amount;

        @TableSignatureField(stored = true)
        private String signature;
    }

    private static class StubFieldHandler implements EncryptedFieldHandler {

        @Override
        public <T> String encrypt(T value) {
            return "ENC(" + value + ")";
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T decrypt(String value, Class<T> rtType) {
            return (T) value.substring(4, value.length() - 1);
        }

        @Override
        public <T> String hmac(T value) {
            return "HMAC(" + value + ")";
        }
    }
}
