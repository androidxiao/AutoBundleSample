package com.black.lib_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wei.
 * Date: 2020-02-02 20:37
 * Description:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AutoBundle {
    boolean exclude() default false;//不参与 intent、bundle 传值
    boolean addFlags() default false;//添加 activity 启动方式
    boolean isCloseFromActivity() default false;//是否关闭 FromActivity
    boolean isBundle() default false;//是否使用 Bundle 对象传值
    boolean isSerializable() default false;//是否是 Serializable 类型
    boolean isParcelable() default false;//是否是 Parcelable 类型
    boolean isParcelableArray() default false;//是否是 ParcelableArray 类型
    boolean isParcelableArrayList() default false;//是否是 ParcelableArrayList 类型
}
