package org.apache.ibatis.enhance.service;

import org.apache.ibatis.enhance.mapper.EnhanceMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 支持表级数据签名与验签的原生 MyBatis Service 契约。
 *
 * <p>方法名、参数、返回值与 {@code mybatis-plus-enhance-spring} 的 {@code IEnhanceService}
 * 对齐，开发者在原生 MyBatis 与 MyBatis-Plus 项目间切换时使用方式一致。"Signed" 方法会在
 * 持久化后生成或更新表签名，并在读取后执行验签。验签失败的处理策略由
 * {@code DataSignatureHandler} 实现决定。</p>
 *
 * <p>与 MyBatis-Plus 版的差异：不继承 {@code IService}（原生无此基类），不提供
 * {@code Wrapper} / {@code IPage} 相关重载（原生无等价概念）。核心 CRUD + 签名语义完整保留。</p>
 *
 * @param <T> 实体类型
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.x
 */
public interface IEnhanceService<T> {

    /**
     * 计算并写回实体的表级签名。
     *
     * @param entity 待签名实体
     * @param <RT>   实体类型
     * @return 签名值发生变化、需要持久化时返回 {@code true}
     */
    <RT> boolean doEntitySignature(RT entity);

    /**
     * 对单个查询结果执行表级验签。
     *
     * @param rowObject   待验签的查询结果
     * @param entityClass 用于解析签名注解的实体类型
     * @param <RT>        结果类型
     */
    <RT> void doSignatureVerification(RT rowObject, Class<?> entityClass);

    /**
     * 插入一条记录，并在同一事务中补签。
     *
     * @param entity 实体对象
     * @return 插入成功返回 {@code true}
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveSigned(T entity) {
        boolean result = getEnhanceMapper().insert(entity) > 0;
        if (result) {
            doSignatureById(getIdValue(entity));
        }
        return result;
    }

    /**
     * 批量插入并在同一事务中补签。
     *
     * @param entityList 实体集合
     * @param batchSize  每批数量
     * @return 全部插入成功返回 {@code true}
     */
    boolean saveBatchSigned(Collection<T> entityList, int batchSize);

    /**
     * 批量插入并在同一事务中补签（默认批次大小）。
     *
     * @param entityList 实体集合
     * @return 全部插入成功返回 {@code true}
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveBatchSigned(Collection<T> entityList) {
        return saveBatchSigned(entityList, 1000);
    }

    /**
     * 根据主键更新，并在同一事务中补签。
     *
     * @param entity 实体对象
     * @return 更新成功返回 {@code true}
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateSignedById(T entity) {
        boolean result = getEnhanceMapper().updateById(entity) > 0;
        if (result) {
            doSignatureById(getIdValue(entity));
        }
        return result;
    }

    /**
     * 根据主键批量更新并在同一事务中补签。
     *
     * @param entityList 实体集合
     * @param batchSize  每批数量
     * @return 全部更新成功返回 {@code true}
     */
    boolean updateBatchSignedById(Collection<T> entityList, int batchSize);

    /**
     * 根据主键批量更新（默认批次大小）。
     *
     * @param entityList 实体集合
     * @return 全部更新成功返回 {@code true}
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatchSignedById(Collection<T> entityList) {
        return updateBatchSignedById(entityList, 1000);
    }

    /**
     * 主键存在则更新，否则插入，并在同一事务中补签。
     *
     * @param entity 实体对象
     * @return 操作成功返回 {@code true}
     */
    @Transactional(rollbackFor = Exception.class)
    boolean saveOrUpdateSigned(T entity);

    /**
     * 根据主键查询并验签。
     *
     * @param id 主键值
     * @return 验签后的实体；不存在时返回 {@code null}
     */
    default T getSignedById(Serializable id) {
        T entity = getEnhanceMapper().selectById(id);
        if (Objects.isNull(entity)) {
            return null;
        }
        doSignatureVerification(entity, entity.getClass());
        return entity;
    }

    /**
     * 根据主键查询验签，返回 {@link Optional}。
     *
     * @param id 主键值
     * @return 验签后的可选实体
     */
    default Optional<T> getSignedOptById(Serializable id) {
        return Optional.ofNullable(getSignedById(id));
    }

    /**
     * 根据主键集合批量查询并验签。
     *
     * @param idList 主键集合
     * @return 验签后的实体列表
     */
    default List<T> listSignedByIds(Collection<? extends Serializable> idList) {
        List<T> list = getEnhanceMapper().selectBatchIds(idList);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(row -> doSignatureVerification(row, row.getClass()));
        }
        return list;
    }

    /**
     * 查询全部记录并验签。
     *
     * @return 验签后的实体列表
     */
    default List<T> listSigned() {
        List<T> list = getEnhanceMapper().selectList();
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(row -> doSignatureVerification(row, row.getClass()));
        }
        return list;
    }

    /**
     * 根据主键对匹配的实体进行表签名（读取原始密文重新计算并写回）。
     *
     * @param id 主键值
     */
    @Transactional(rollbackFor = Exception.class)
    void doSignatureById(Serializable id);

    /**
     * 根据主键集合批量补签。
     *
     * @param idList 主键集合
     */
    @Transactional(rollbackFor = Exception.class)
    void doSignatureByBatchIds(Collection<? extends Serializable> idList);

    /**
     * 根据主键查询原始密文并验签（不写回）。
     *
     * @param id 主键值
     */
    @Transactional(rollbackFor = Exception.class)
    default void doSignatureVerificationById(Serializable id) {
        T entity = getEnhanceMapper().selectIgnoreDecryptById(id);
        if (Objects.nonNull(entity)) {
            doSignatureVerification(entity, entity.getClass());
        }
    }

    /**
     * 获取对应的增强 Mapper。
     *
     * @return 增强 Mapper
     */
    EnhanceMapper<T> getEnhanceMapper();

    /**
     * 从实体中提取主键值，用于写入后回查补签。
     *
     * <p>子类可覆盖此方法以适配非标准主键字段名。</p>
     *
     * @param entity 实体对象
     * @return 主键值
     */
    Serializable getIdValue(T entity);
}
