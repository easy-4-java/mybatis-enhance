package org.apache.ibatis.utils;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @see org.apache.ibatis.reflection.SystemMetaObject
 */
abstract @Deprecated
public class MetaObjectUtils {

    /* 默认对象工厂 */
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    /* 默认对象包装工厂 */
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());

    private MetaObjectUtils() {
        // Prevent Instantiation of Static Class
    }

    /**
     * 完成 {@code forObject} 对应的框架处理。
     *
     * @param object 目标对象
     * @return 处理结果
     */
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
    }

    /**
     * 完成 {@code forObject} 对应的框架处理。
     *
     * @param object        目标对象
     * @param objectFactory 对象工厂
     * @return 处理结果
     */
    public static MetaObject forObject(Object object, ObjectFactory objectFactory) {
        return MetaObject.forObject(object, objectFactory, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
    }

    /**
     * 完成 {@code forObject} 对应的框架处理。
     *
     * @param object               目标对象
     * @param objectFactory        对象工厂
     * @param objectWrapperFactory 调用参数 {@code objectWrapperFactory}
     * @return 处理结果
     */
    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, new DefaultReflectorFactory());
    }

    /**
     * 完成 {@code forObject} 对应的框架处理。
     *
     * @param object               目标对象
     * @param objectFactory        对象工厂
     * @param objectWrapperFactory 调用参数 {@code objectWrapperFactory}
     * @param reflectorFactory     反射器工厂
     * @return 处理结果
     */
    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    private static class NullObject {
    }

}
