package org.apache.ibatis.enhance.service.impl;

import lombok.Getter;
import org.apache.ibatis.enhance.mapper.EnhanceMapper;
import org.apache.ibatis.enhance.service.IEnhanceService;
import org.apache.ibatis.enhance.util.TableFieldHelper;
import org.apache.ibatis.enhance.crypto.handler.DataSignatureHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * {@link IEnhanceService} 的抽象基础实现。
 *
 * <p>把签名和验签流程与原生 MyBatis Mapper 组合，子类只需声明具体 Mapper 和实体类型，
 * 并通过构造器注入 {@link DataSignatureHandler}。批量写入和补签方法在事务中调用，
 * 保证业务数据与签名原子提交。</p>
 *
 * <p>与 {@code mybatis-plus-enhance-spring} 的 {@code EnhanceServiceImpl} 的差异：
 * <ul>
 *   <li>不继承 MP 的 {@code ServiceImpl}（原生无此基类），Mapper 由子类通过
 *       {@link #getBaseMapper()} 提供（通常配合 Spring {@code @Autowired} 注入）</li>
 *   <li>批量写入用简单循环（原生无 MP 的 {@code executeBatch} + SqlSession flush 机制）</li>
 *   <li>主键提取用反射读 {@code id} 字段，子类可覆盖 {@link #getIdValue(Object)} 适配非标准主键</li>
 * </ul>
 * 过程不同，但方法签名和结果语义与 Plus 版完全一致。</p>
 *
 * @param <M> 实体对应的增强 Mapper 类型
 * @param <T> 实体类型
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public abstract class EnhanceServiceImpl<M extends EnhanceMapper<T>, T> implements IEnhanceService<T> {

    /**
     * 数据签名和验签 Handler。
     */
    @Getter
    protected final DataSignatureHandler dataSignatureHandler;

    public EnhanceServiceImpl(DataSignatureHandler dataSignatureHandler) {
        this.dataSignatureHandler = Objects.requireNonNull(dataSignatureHandler,
                "DataSignatureHandler must not be null");
    }

    /**
     * 子类提供具体的增强 Mapper 实例。
     *
     * <p>典型实现：{@code @Autowired private UserMapper userMapper; protected UserMapper getBaseMapper() { return userMapper; }}</p>
     *
     * @return 当前 Service 绑定的增强 Mapper
     */
    protected abstract M getBaseMapper();

    @Override
    public M getEnhanceMapper() {
        return getBaseMapper();
    }

    @Override
    public <RT> boolean doEntitySignature(RT entity) {
        return getDataSignatureHandler().doEntitySignature(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <RT> void doSignatureVerification(RT rowObject, Class<?> entityClass) {
        getDataSignatureHandler().doSignatureVerification(rowObject, entityClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchSigned(Collection<T> entityList, int batchSize) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        M mapper = getBaseMapper();
        List<Serializable> idBucket = new ArrayList<>(Math.min(batchSize, entityList.size()));
        for (T entity : entityList) {
            mapper.insert(entity);
            idBucket.add(getIdValue(entity));
            if (idBucket.size() >= batchSize) {
                doSignatureByBatchIds(idBucket);
                idBucket.clear();
            }
        }
        // 补签不足一个批次的剩余记录
        if (!CollectionUtils.isEmpty(idBucket)) {
            doSignatureByBatchIds(idBucket);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchSignedById(Collection<T> entityList, int batchSize) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        M mapper = getBaseMapper();
        List<Serializable> idBucket = new ArrayList<>(Math.min(batchSize, entityList.size()));
        for (T entity : entityList) {
            mapper.updateById(entity);
            idBucket.add(getIdValue(entity));
            if (idBucket.size() >= batchSize) {
                doSignatureByBatchIds(idBucket);
                idBucket.clear();
            }
        }
        if (!CollectionUtils.isEmpty(idBucket)) {
            doSignatureByBatchIds(idBucket);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateSigned(T entity) {
        Serializable id = getIdValue(entity);
        M mapper = getBaseMapper();
        boolean result;
        if (Objects.isNull(id)) {
            result = mapper.insert(entity) > 0;
        } else {
            result = mapper.updateById(entity) > 0;
        }
        if (result) {
            doSignatureById(getIdValue(entity));
        }
        return result;
    }

    @Override
    public void doSignatureById(Serializable id) {
        // 1、根据 ID 查询原始数据（不解密）
        T entity = getEnhanceMapper().selectIgnoreDecryptById(id);
        if (Objects.nonNull(entity)) {
            // 2、对原始数据进行签名
            boolean doUpdate = this.doEntitySignature(entity);
            // 3、签名值变化时更新
            if (doUpdate) {
                getBaseMapper().updateById(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doSignatureByBatchIds(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        // 1、批量查询原始数据
        List<T> list = getEnhanceMapper().selectIgnoreDecryptBatchIds(idList);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 2、逐条签名并收集需要更新的实体
        List<T> toUpdate = new ArrayList<>(list.size());
        for (T entity : list) {
            if (this.doEntitySignature(entity)) {
                toUpdate.add(entity);
            }
        }
        // 3、批量更新
        for (T entity : toUpdate) {
            getBaseMapper().updateById(entity);
        }
    }

    @Override
    public Serializable getIdValue(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        // 默认反射读 "id" 字段；子类可覆盖适配非标准主键名
        for (Field field : TableFieldHelper.getFields(entity.getClass())) {
            if ("id".equals(field.getName())) {
                Object value = TableFieldHelper.readValue(entity, field);
                if (value instanceof Serializable) {
                    return (Serializable) value;
                }
                if (Objects.nonNull(value)) {
                    throw new IllegalStateException("主键字段 id 不是 Serializable：" + value.getClass());
                }
                return null;
            }
        }
        throw new IllegalStateException("实体 " + entity.getClass().getName()
                + " 未找到 id 字段，请覆盖 getIdValue 方法指定主键");
    }
}
