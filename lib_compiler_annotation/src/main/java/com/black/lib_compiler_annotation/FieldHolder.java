package com.black.lib_compiler_annotation;

import javax.lang.model.type.TypeMirror;

/**
 * Created by wei.
 * Date: 2020-02-02 20:41
 * Description: FieldHolder 保存了每个变量类型、变量名、注解等信息。
 */
public class FieldHolder {

    private String variableName;//变量名
    private TypeMirror clazz;//字段类型(如：String)
    private String packageName;//包名
    private boolean addFlags;//是否是添加 activity 启动方式
    private boolean exclude;//是否参与 intent、bundle 传值
    private boolean closeFromActivity;//是否关闭当前 Activity
    private boolean isBundle;//是否使用 Bundle 传值
    private boolean isSerializable;//是否实现 Serializable 接口的类
    private boolean isParcelable;//自定义类实现 Parcelable 接口
    private boolean isParcelableArray;//自定义类 ParcelableArray 类型
    private boolean isParcelableArrayList;//自定义类 ParcelableArrayList 类型

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public TypeMirror getClazz() {
        return clazz;
    }

    public void setClazz(TypeMirror clazz) {
        this.clazz = clazz;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isCloseFromActivity() {
        return closeFromActivity;
    }

    public void setCloseFromActivity(boolean closeFromActivity) {
        this.closeFromActivity = closeFromActivity;
    }

    public boolean getAddFlags() {
        return addFlags;
    }

    public void setAddFlags(boolean addFlags) {
        this.addFlags = addFlags;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public boolean isBundle() {
        return isBundle;
    }

    public void setBundle(boolean bundle) {
        isBundle = bundle;
    }

    public boolean isSerializable() {
        return isSerializable;
    }

    public void setSerializable(boolean serializable) {
        isSerializable = serializable;
    }

    public boolean isParcelable() {
        return isParcelable;
    }

    public void setParcelable(boolean parcelable) {
        isParcelable = parcelable;
    }

    public boolean isParcelableArray() {
        return isParcelableArray;
    }

    public void setParcelableArray(boolean parcelableArray) {
        isParcelableArray = parcelableArray;
    }

    public boolean isParcelableArrayList() {
        return isParcelableArrayList;
    }

    public void setParcelableArrayList(boolean parcelableArrayList) {
        isParcelableArrayList = parcelableArrayList;
    }
}
