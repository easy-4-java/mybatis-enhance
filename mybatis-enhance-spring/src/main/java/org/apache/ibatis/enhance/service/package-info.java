/**
 * Spring 集成：基于 Spring 事务的增强 Service 契约。
 *
 * <h2>v2.x 重构：与 mybatis-plus-enhance-spring 对齐</h2>
 *
 * <p>v2.x 起，本模块的类与 {@code com.baomidou.mybatisplus.enhance.service.*}（Plus 版）同名同形态，
 * 但底层不依赖 MyBatis-Plus；切换框架时仅换依赖 {@code <artifactId>} 即可。</p>
 *
 * <table>
 *   <caption>类对照表</caption>
 *   <tr><th>mybatis-enhance（本模块）</th><th>mybatis-plus-enhance 对应</th></tr>
 *   <tr><td>{@code org.apache.ibatis.enhance.service.IEnhanceService}</td>
 *       <td>{@code com.baomidou.mybatisplus.enhance.service.IEnhanceService}</td></tr>
 *   <tr><td>{@code org.apache.ibatis.enhance.service.impl.EnhanceServiceImpl}</td>
 *       <td>{@code com.baomidou.mybatisplus.enhance.service.impl.EnhanceServiceImpl}</td></tr>
 * </table>
 *
 * <h3>使用差异（实现层）</h3>
 *
 * <p>Plus 版借由 {@code IService} 与 {@code ServiceImpl} 基类屏蔽 MyBatis-Plus 细节；本模块走
 * 原生 MyBatis，所以子类需要手动持有 {@code EnhanceMapper}（依赖注入或自建）。除此之外，方法
 * 签名、返回值与使用时机一致。</p>
 *
 * <h3>迁移指引</h3>
 *
 * <ul>
 *   <li>从一个框架切换到另一个时，业务代码只需替换 import 包名。</li>
 *   <li>非签名 / 验签相关方法（{@code saveSigned} / {@code getSignedById} / {@code doSignatureById} 等）保持
 *       行为一致——尽管 MyBatis（非 Plus）与 MyBatis-Plus 端的实现机制不同。</li>
 *   <li>Plus 专属 API（{@code IService.batchByIds}、{@code IPage}、{@code LambdaQueryWrapper}）在本模块
 *       没有等价物；如需复杂条件构造器，请在 native MyBatis 中手写 {@code @SelectProvider}。</li>
 * </ul>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 2.0.x
 */
package org.apache.ibatis.enhance.service;
