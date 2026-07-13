package org.apache.ibatis.enhance.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.enhance.annotation.crypto.IgnoreEncrypted;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 原生 MyBatis 增强 Mapper 基类。
 *
 * <p>定义签名/验签流程需要的核心 CRUD 与"查询原始密文"方法。与 MyBatis-Plus 的
 * {@code EnhanceBaseMapper} 心智模型一致：使用方让自己的 Mapper 继承本接口，并在
 * Mapper XML 或注解中实现这些方法。</p>
 *
 * <p>与 MyBatis-Plus 的差异：原生 MyBatis 没有自动 SQL 注入，所有方法必须由开发者
 * 通过 XML 或 {@code @Insert/@Select/@Update} 注解提供实现。参考用法：</p>
 *
 * <pre>
 * public interface UserMapper extends EnhanceMapper&lt;UserEntity&gt; { }
 * </pre>
 *
 * <pre>
 * &lt;!-- UserMapper.xml --&gt;
 * &lt;insert id="insert"&gt;INSERT INTO user(name, age) VALUES(#{name}, #{age})&lt;/insert&gt;
 * &lt;update id="updateById"&gt;UPDATE user SET name=#{name}, age=#{age} WHERE id=#{id}&lt;/update&gt;
 * &lt;select id="selectById" resultType="UserEntity"&gt;SELECT * FROM user WHERE id=#{id}&lt;/select&gt;
 * &lt;select id="selectIgnoreDecryptById" resultType="UserEntity"&gt;SELECT * FROM user WHERE id=#{id}&lt;/select&gt;
 * </pre>
 *
 * <p>{@link IgnoreEncrypted} 标注的方法会被 {@code DataDecryptionInterceptor} 跳过，
 * 返回数据库原始密文，供签名/验签流程读取真实字段值。</p>
 *
 * @param <T> 实体类型
 * @author <a href="https://github.com/hiwepy">wandl</a>
 * @since 1.0.x
 */
public interface EnhanceMapper<T> {

    /**
     * 插入一条记录。
     *
     * @param entity 实体对象
     * @return 受影响行数
     */
    int insert(T entity);

    /**
     * 根据主键更新记录。
     *
     * @param entity 实体对象（须含主键）
     * @return 受影响行数
     */
    int updateById(T entity);

    /**
     * 根据主键查询。
     *
     * @param id 主键值
     * @return 实体对象；不存在时返回 {@code null}
     */
    T selectById(Serializable id);

    /**
     * 根据主键集合批量查询。
     *
     * @param idList 主键集合（不能为 null 或空）
     * @return 实体列表
     */
    List<T> selectBatchIds(@Param("list") Collection<? extends Serializable> idList);

    /**
     * 查询全部记录。
     *
     * @return 实体列表
     */
    List<T> selectList();

    /**
     * 根据主键查询原始密文（不执行结果解密）。
     *
     * <p>签名流程依赖此方法读取真实字段值重新计算签名。{@link IgnoreEncrypted} 保证
     * 查询结果不被 {@code DataDecryptionInterceptor} 解密。</p>
     *
     * @param id 主键值
     * @return 原始密文实体；不存在时返回 {@code null}
     */
    @IgnoreEncrypted
    T selectIgnoreDecryptById(Serializable id);

    /**
     * 根据主键集合批量查询原始密文（不执行结果解密）。
     *
     * @param idList 主键集合
     * @return 原始密文实体列表
     */
    @IgnoreEncrypted
    List<T> selectIgnoreDecryptBatchIds(@Param("list") Collection<? extends Serializable> idList);
}
