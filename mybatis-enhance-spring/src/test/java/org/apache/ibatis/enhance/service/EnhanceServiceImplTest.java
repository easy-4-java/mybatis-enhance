package org.apache.ibatis.enhance.service;

import org.apache.ibatis.enhance.mapper.EnhanceMapper;
import org.apache.ibatis.enhance.service.impl.EnhanceServiceImpl;
import org.apache.ibatis.enhance.crypto.handler.DataSignatureHandler;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证 {@link EnhanceServiceImpl} 的签名/验签流程编排逻辑（不依赖数据库）。
 *
 * <p>用内存 mock Mapper 记录调用顺序，断言写入→补签、查询→验签的编排正确性。</p>
 */
public class EnhanceServiceImplTest {

    @Test
    public void saveSignedShouldInsertThenSignById() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        User user = new User(1L, "张三");
        boolean result = service.saveSigned(user);

        Assert.assertTrue(result);
        // 先 insert，再按 id 补签（补签内部 selectIgnoreDecryptById → doEntitySignature）
        // 默认 handler 的 doEntitySignature 返回 false，故不再 updateById
        Assert.assertEquals(Arrays.asList("insert(1)", "selectIgnoreDecryptById(1)"), mapper.calls);
        Assert.assertEquals(1, handler.signatureCount.get());
    }

    @Test
    public void getSignedByIdShouldVerifyWhenEntityExists() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        mapper.stored.put(1L, new User(1L, "张三"));
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        User result = service.getSignedById(1L);

        Assert.assertNotNull(result);
        Assert.assertEquals("张三", result.name);
        Assert.assertEquals(1, handler.verifyCount.get());
    }

    @Test
    public void getSignedByIdShouldReturnNullWithoutVerifyWhenAbsent() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        User result = service.getSignedById(999L);

        Assert.assertNull(result);
        Assert.assertEquals(0, handler.verifyCount.get());
    }

    @Test
    public void listSignedByIdsShouldVerifyEachRow() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        mapper.stored.put(1L, new User(1L, "甲"));
        mapper.stored.put(2L, new User(2L, "乙"));
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        List<User> result = service.listSignedByIds(Arrays.asList(1L, 2L));

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(2, handler.verifyCount.get());
    }

    @Test
    public void doSignatureByIdShouldReadRawSignAndUpdate() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        mapper.stored.put(1L, new User(1L, "张三"));
        CountingHandler handler = new CountingHandler(true); // doEntitySignature 返回 true
        UserService<User> service = new UserService<>(mapper, handler);

        service.doSignatureById(1L);

        Assert.assertEquals(Arrays.asList("selectIgnoreDecryptById(1)", "updateById(1)"), mapper.calls);
    }

    @Test
    public void doSignatureByIdShouldSkipUpdateWhenSignatureUnchanged() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        mapper.stored.put(1L, new User(1L, "张三"));
        CountingHandler handler = new CountingHandler(false); // doEntitySignature 返回 false
        UserService<User> service = new UserService<>(mapper, handler);

        service.doSignatureById(1L);

        Assert.assertEquals(Arrays.asList("selectIgnoreDecryptById(1)"), mapper.calls);
    }

    @Test
    public void saveBatchSignedShouldInsertAllThenSignBatch() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler(true);
        UserService<User> service = new UserService<>(mapper, handler);

        boolean result = service.saveBatchSigned(Arrays.asList(
                new User(1L, "甲"), new User(2L, "乙")), 10);

        Assert.assertTrue(result);
        Assert.assertTrue(mapper.calls.contains("insert(1)"));
        Assert.assertTrue(mapper.calls.contains("insert(2)"));
        // 批量补签走 selectIgnoreDecryptBatchIds
        Assert.assertTrue(mapper.calls.get(2).startsWith("selectIgnoreDecryptBatchIds"));
    }

    @Test
    public void saveBatchSignedShouldSignByConfiguredBatchSize() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        boolean result = service.saveBatchSigned(Arrays.asList(
                new User(1L, "甲"), new User(2L, "乙"), new User(3L, "丙")), 2);

        Assert.assertTrue(result);
        Assert.assertEquals(Arrays.asList(
                "insert(1)",
                "insert(2)",
                "selectIgnoreDecryptBatchIds([1, 2])",
                "insert(3)",
                "selectIgnoreDecryptBatchIds([3])"), mapper.calls);
        Assert.assertEquals(3, handler.signatureCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveBatchSignedShouldRejectNonPositiveBatchSize() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        service.saveBatchSigned(Arrays.asList(new User(1L, "甲")), 0);
    }

    @Test
    public void updateBatchSignedByIdShouldSignByConfiguredBatchSize() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        boolean result = service.updateBatchSignedById(Arrays.asList(
                new User(1L, "甲"), new User(2L, "乙"), new User(3L, "丙")), 2);

        Assert.assertTrue(result);
        Assert.assertEquals(Arrays.asList(
                "updateById(1)",
                "updateById(2)",
                "selectIgnoreDecryptBatchIds([1, 2])",
                "updateById(3)",
                "selectIgnoreDecryptBatchIds([3])"), mapper.calls);
        Assert.assertEquals(3, handler.signatureCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBatchSignedByIdShouldRejectNonPositiveBatchSize() {
        TrackingMapper<User> mapper = new TrackingMapper<>();
        CountingHandler handler = new CountingHandler();
        UserService<User> service = new UserService<>(mapper, handler);

        service.updateBatchSignedById(Arrays.asList(new User(1L, "甲")), 0);
    }

    // ===== 测试辅助类 =====

    static class User {
        Long id;
        String name;

        User(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /** 测试用 Service 子类，覆盖 getBaseMapper 和 getIdValue。 */
    static class UserService<T extends User> extends EnhanceServiceImpl<TrackingMapper<T>, T> {
        final TrackingMapper<T> mapper;

        UserService(TrackingMapper<T> mapper, DataSignatureHandler handler) {
            super(handler);
            this.mapper = mapper;
        }

        @Override
        protected TrackingMapper<T> getBaseMapper() {
            return mapper;
        }

        @Override
        public Serializable getIdValue(T entity) {
            return entity.id;
        }
    }

    /** 内存 mock Mapper，记录调用轨迹。 */
    static class TrackingMapper<T extends User> implements EnhanceMapper<T> {
        final List<String> calls = new ArrayList<>();
        final java.util.Map<Serializable, T> stored = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public int insert(T entity) {
            calls.add("insert(" + entity.id + ")");
            stored.put(entity.id, entity);
            return 1;
        }

        @Override
        public int updateById(T entity) {
            calls.add("updateById(" + entity.id + ")");
            stored.put(entity.id, entity);
            return 1;
        }

        @Override
        public T selectById(Serializable id) {
            return stored.get(id);
        }

        @Override
        public List<T> selectBatchIds(Collection<? extends Serializable> idList) {
            List<T> result = new ArrayList<>();
            for (Serializable id : idList) {
                T e = stored.get(id);
                if (Objects.nonNull(e)) {
                    result.add(e);
                }
            }
            return result;
        }

        @Override
        public List<T> selectList() {
            return new ArrayList<>(stored.values());
        }

        @Override
        public T selectIgnoreDecryptById(Serializable id) {
            calls.add("selectIgnoreDecryptById(" + id + ")");
            return stored.get(id);
        }

        @Override
        public List<T> selectIgnoreDecryptBatchIds(Collection<? extends Serializable> idList) {
            calls.add("selectIgnoreDecryptBatchIds(" + idList + ")");
            List<T> result = new ArrayList<>();
            for (Serializable id : idList) {
                T e = stored.get(id);
                if (Objects.nonNull(e)) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    /** 计数 mock Handler，记录签名/验签次数。 */
    static class CountingHandler implements DataSignatureHandler {
        final AtomicInteger signatureCount = new AtomicInteger();
        final AtomicInteger verifyCount = new AtomicInteger();
        final boolean signResult;

        CountingHandler() {
            this(false);
        }

        CountingHandler(boolean signResult) {
            this.signResult = signResult;
        }

        @Override
        public boolean doEntitySignature(Object entity) {
            signatureCount.incrementAndGet();
            return signResult;
        }

        @Override
        public void doSignatureVerification(Object rawObject, Class<?> entityClass) {
            verifyCount.incrementAndGet();
        }
    }
}
