package com.black.lib_compiler_annotation;


import com.black.lib_annotation.AutoBundle;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


/**
 * Created by wei.
 * Date: 2020/1/18 10:45
 * Desc:
 */
public class AutoBundleProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;
    private Map<String, List<FieldHolder>> mFieldHolders = new HashMap<>();
    private String mAutoBundleField;//Activity 中 Bundle 的变量名


    @Override
    public synchronized void init(ProcessingEnvironment ev) {
        super.init(ev);
        mElementUtils = ev.getElementUtils();
        mFiler = ev.getFiler();
        mMessager = ev.getMessager();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "--------------AutoBundleProcessor--------------");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> sets = new LinkedHashSet<>();
        sets.add(AutoBundle.class.getCanonicalName());
        return sets;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment rev) {
        mFieldHolders.clear();
        collectInfo(rev);
        writeToFile();
        return true;
    }

    private void collectInfo(RoundEnvironment rev) {
        Set<? extends Element> elementsAnnotatedWith = rev.getElementsAnnotatedWith(AutoBundle.class);
        for (Element element : elementsAnnotatedWith) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                String className = typeElement.getSimpleName().toString();
                String packageName = mElementUtils.getPackageOf(typeElement).getQualifiedName().toString();

                AutoBundle autoBundle = variableElement.getAnnotation(AutoBundle.class);
                //变量名
                Name simpleName = variableElement.getSimpleName();
                //变量类型
                TypeMirror typeMirror = variableElement.asType();

                List<FieldHolder> fieldHolders = mFieldHolders.get(className);
                if (fieldHolders == null) {
                    fieldHolders = new ArrayList<>();
                    mFieldHolders.put(className, fieldHolders);
                }
                FieldHolder fieldHolder = new FieldHolder();
                fieldHolder.setVariableName(simpleName.toString());
                fieldHolder.setClazz(typeMirror);
                fieldHolder.setPackageName(packageName);
                fieldHolder.setCloseFromActivity(autoBundle.isCloseFromActivity());
                fieldHolder.setBundle(autoBundle.isBundle());
                fieldHolder.setExclude(autoBundle.exclude());
                fieldHolder.setAddFlags(autoBundle.addFlags());
                fieldHolder.setSerializable(autoBundle.isSerializable());
                fieldHolder.setParcelable(autoBundle.isParcelable());
                fieldHolder.setParcelableArray(autoBundle.isParcelableArray());
                fieldHolder.setParcelableArrayList(autoBundle.isParcelableArrayList());

                fieldHolders.add(fieldHolder);
            }
        }
    }

    private void writeToFile() {

        String packageName = "";

        Set<Map.Entry<String, List<FieldHolder>>> entries = mFieldHolders.entrySet();
        Iterator<Map.Entry<String, List<FieldHolder>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<FieldHolder>> next = iterator.next();
            String clazzName = next.getKey();
            TypeSpec.Builder typeClass = TypeSpec.classBuilder(clazzName + "AutoBundle");
            typeClass.addJavadoc("This codes are generated automatically. Do not modify!");
            List<FieldHolder> fieldHolders = next.getValue();
            for (FieldHolder fieldHolder : fieldHolders) {
                packageName = fieldHolder.getPackageName();
                FieldSpec builder = FieldSpec.builder(ClassName.get(fieldHolder.getClazz()), fieldHolder.getVariableName(), Modifier.PRIVATE).build();
                typeClass.addField(builder);
                MethodSpec.Builder buildParamMethod = MethodSpec.methodBuilder(String.format("%s", fieldHolder.getVariableName()));
                buildParamMethod.addParameter(ClassName.get(fieldHolder.getClazz()), fieldHolder.getVariableName());
                buildParamMethod.addStatement(String.format("this.%s=%s", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                buildParamMethod.addStatement("return this");
                buildParamMethod.addModifiers(Modifier.PUBLIC);
                buildParamMethod.returns(ClassName.get(fieldHolder.getPackageName(), clazzName + "AutoBundle"));
                typeClass.addMethod(buildParamMethod.build());
            }

            MethodSpec.Builder builderStart = generateStart(fieldHolders, clazzName, "start");
            MethodSpec.Builder builderStartForResult = generateStartForResult(fieldHolders, clazzName, "startForResult");

            /******************************** getIntent ******************************/
            MethodSpec.Builder bindIntentMethod = MethodSpec.methodBuilder("bindIntent")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class);
            bindIntentMethod.addParameter(ClassName.get(packageName, clazzName), "target");
            bindIntentMethod.addParameter(ClassName.get("android.content", "Intent"), "intent");
            String bundleStr = null;
            /** 获取 intent 值 */
            for (FieldHolder fieldHolder : fieldHolders) {
                packageName = fieldHolder.getPackageName();
                TypeMirror clazz = fieldHolder.getClazz();
                String fieldType = clazz.toString();
                if ((boolean.class.getName().equals(fieldType) || Boolean.class.getName().equals(fieldType)) && !fieldHolder.isBundle()&&!fieldHolder.isExclude()) {
                    bindIntentMethod.addStatement(String.format("target.%s = intent.getBooleanExtra(\"%s\",false)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((byte.class.getName().equals(fieldType) || Byte.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s = intent.getByteExtra(\"%s\",(byte)0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((char.class.getName().equals(fieldType) || Character.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s = intent.getCharExtra(\"%s\",(char)0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((short.class.getName().equals(fieldType) || Short.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s = intent.getShortExtra(\"%s\",(short)0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((int.class.getName().equals(fieldType) || Integer.class.getName().equals(fieldType)) && !fieldHolder.isBundle()&&!fieldHolder.isExclude()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getIntExtra(\"%s\",0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((long.class.getName().equals(fieldType) || Long.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getLongExtra(\"%s\",0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((float.class.getName().equals(fieldType) || Float.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getFloatExtra(\"%s\",0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((double.class.getName().equals(fieldType) || Double.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getDoubleExtra(\"%s\",0)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((CharSequence.class.getName().equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getCharSequenceExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((fieldHolder.isParcelable()) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=(%s)intent.getParcelableExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getClazz(), fieldHolder.getVariableName()));
                } else if ((fieldHolder.isParcelableArray()) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("Parcelable[] temp =intent.getParcelableArrayExtra(\"%s\")", fieldHolder.getVariableName()));
                    bindIntentMethod.addCode("if(temp!=null&&temp.length>0){\n");
                    bindIntentMethod.addStatement(String.format("target.%s = java.util.Arrays.copyOf(temp, temp.length,%s)", fieldHolder.getVariableName(), fieldHolder.getClazz() + ".class"));
                    bindIntentMethod.addCode("}\n");
                } else if ((fieldHolder.isParcelableArrayList()) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getParcelableArrayListExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("java.util.ArrayList<java.lang.Integer>".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getIntegerArrayListExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("java.util.ArrayList<java.lang.String>".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getStringArrayListExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("java.util.ArrayList<java.lang.CharSequence>".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getCharSequenceArrayListExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ((fieldHolder.isSerializable()) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=(%s)intent.getSerializableExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getClazz(), fieldHolder.getVariableName()));
                } else if (("boolean[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getBooleanArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("byte[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getByteArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("short[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getShortArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("char[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getCharArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("int[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getIntArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("long[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getLongArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("float[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getFloatArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("double[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getDoubleArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("java.lang.String[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getStringArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (String.class.getName().equals(fieldType) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getStringExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if (("java.lang.CharSequence[]".equals(fieldType)) && !fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getCharSequenceArrayExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else if ("android.os.Bundle".equals(fieldType)) {
                    bindIntentMethod.addStatement(String.format("target.%s=intent.getBundleExtra(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                    bundleStr = "target." + fieldHolder.getVariableName();
                } else if ((boolean.class.getName().equals(fieldType) || Boolean.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getBoolean(\"%s\",false)", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));
                } else if (byte.class.getName().equals(fieldType) || Byte.class.getName().equals(fieldType)) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getByte(\"%s\",(byte)0)", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (char.class.getName().equals(fieldType) || Character.class.getName().equals(fieldType)) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getChar(\"%s\",(char)0)", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((short.class.getName().equals(fieldType) || Short.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getShort(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((int.class.getName().equals(fieldType) || Integer.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getInt(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((long.class.getName().equals(fieldType) || Long.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getLong(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((float.class.getName().equals(fieldType) || Float.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getFloat(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((double.class.getName().equals(fieldType) || Double.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {

                    bindIntentMethod.addStatement(String.format("target.%s=%s.getDouble(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));
                } else if ((CharSequence.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getCharSequence(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((fieldHolder.isParcelable()) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getParcelable(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((fieldHolder.isParcelableArray()) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("Parcelable[] tempBundle =%s.getParcelableArray(\"%s\")", bundleStr, fieldHolder.getVariableName()));
                    bindIntentMethod.addCode("if(tempBundle!=null&&tempBundle.length>0){\n");
                    bindIntentMethod.addStatement(String.format("target.%s = java.util.Arrays.copyOf(tempBundle, tempBundle.length,%s)", fieldHolder.getVariableName(), fieldHolder.getClazz() + ".class"));
                    bindIntentMethod.addCode("}\n");

                } else if ((fieldHolder.isParcelableArrayList()) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getParcelableArrayList(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));
                } else if (("java.util.ArrayList<java.lang.Integer>".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getIntegerArrayList(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (("java.util.ArrayList<java.lang.String>".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getStringArrayList(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (("java.util.ArrayList<java.lang.CharSequence>".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getCharSequenceArrayList(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ((fieldHolder.isSerializable()) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=(%s)%s.getSerializable(\"%s\")", fieldHolder.getVariableName(), fieldHolder.getClazz(), bundleStr, fieldHolder.getVariableName()));

                } else if (("boolean[]".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getBooleanArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (("byte[]".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getByteArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (("short[]".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getShortArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (("char[]".equals(fieldType)) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getCharArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ("int[]".equals(fieldType) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getIntArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ("long[]".equals(fieldType) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getLongArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ("float[]".equals(fieldType) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getFloatArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ("double[]".equals(fieldType) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getDoubleArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ("java.lang.String[]".equals(fieldType) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getStringArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if ("java.lang.CharSequence[]".equals(fieldType) && fieldHolder.isBundle()) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getCharSequenceArray(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));

                } else if (fieldHolder.isBundle() && String.class.getName().equals(fieldType)) {
                    bindIntentMethod.addStatement(String.format("target.%s=%s.getString(\"%s\")", fieldHolder.getVariableName(), bundleStr, fieldHolder.getVariableName()));
                }

            }
            typeClass.addMethod(builderStart.build());
            typeClass.addMethod(builderStartForResult.build());
            typeClass.addMethod(bindIntentMethod.build());
            typeClass.addModifiers(Modifier.PUBLIC);

            /**************************************************************/


            //与目标 Class 放在同一个包下，解决 Class 属性的可访问性
            JavaFile javaFile = JavaFile.builder(packageName, typeClass.build())
                    .build();
            try {
                //生成 class 文件
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void generateCommonStart(MethodSpec.Builder builderMethod, List<FieldHolder> fieldHolders, String clazzName) {

        builderMethod.addStatement(String.format("Intent intent = new Intent(context,%s.class)", clazzName));
        /** 生成页面跳转方法 */

        for (FieldHolder fieldHolder : fieldHolders) {
            String fieldType = fieldHolder.getClazz().toString();
            if ("android.os.Bundle".equals(fieldType)) {
                builderMethod.addStatement(String.format("Bundle %s = new Bundle()", fieldHolder.getVariableName()));
                builderMethod.addStatement(String.format("intent.putExtra(\"%s\",%s)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                mAutoBundleField = fieldHolder.getVariableName();
            } else if (fieldHolder.isBundle() && String.class.getName().equals(fieldType)) {
                builderMethod.addStatement(String.format("%s.putString(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));
            } else if ((boolean.class.getName().equals(fieldType) || Boolean.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putBoolean(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));
            } else if ((byte.class.getName().equals(fieldType) || Byte.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putByte(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((char.class.getName().equals(fieldType) || Character.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putChar(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((short.class.getName().equals(fieldType) || Short.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putShort(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((int.class.getName().equals(fieldType) || Integer.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putInt(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((long.class.getName().equals(fieldType) || Long.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putLong(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((float.class.getName().equals(fieldType) || Float.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putFloat(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((double.class.getName().equals(fieldType) || Double.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putDouble(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((CharSequence.class.getName().equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putCharSequence(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((fieldHolder.isParcelable()) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putParcelable(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((fieldHolder.isParcelableArray()) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putParcelableArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (fieldHolder.isParcelableArrayList() && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putParcelableArrayList(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (("java.util.ArrayList<java.lang.Integer>".equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putIntegerArrayList(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (("java.util.ArrayList<java.lang.String>".equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putStringArrayList(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (("java.util.ArrayList<java.lang.CharSequence>".equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putCharSequenceArrayList(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ((fieldHolder.isSerializable()) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putSerializable(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (("boolean[]".equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putBooleanArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (("byte[]".equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putByteArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if (("short[]".equals(fieldType)) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putShortArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("char[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putCharArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("int[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putIntArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("long[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putLongArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("float[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putFloatArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("double[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putDoubleArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("java.lang.String[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putStringArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else if ("java.lang.CharSequence[]".equals(fieldType) && fieldHolder.isBundle()) {
                builderMethod.addStatement(String.format("%s.putCharSequenceArray(\"%s\",%s)", mAutoBundleField, fieldHolder.getVariableName(), fieldHolder.getVariableName()));

            } else {
                if (fieldHolder.isSerializable()) {
                    builderMethod.addStatement(String.format("intent.putExtra(\"%s\",(java.io.Serializable)%s)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                } else {
                    if(!fieldHolder.isExclude())
                    builderMethod.addStatement(String.format("intent.putExtra(\"%s\",%s)", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
                }
            }
        }

    }

    private MethodSpec.Builder generateStart(List<FieldHolder> fieldHolders, String clazzName, String methodName) {
        MethodSpec.Builder builderMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        builderMethod.addParameter(ClassName.get("android.content", "Context"), "context");
        generateCommonStart(builderMethod, fieldHolders, clazzName);
        generateAddFlags(fieldHolders, builderMethod);
        generateIsCloseActivity(fieldHolders, builderMethod);
        builderMethod.addStatement("context.startActivity(intent)");
        return builderMethod;

    }

    private MethodSpec.Builder generateStartForResult(List<FieldHolder> fieldHolders, String clazzName, String methodName) {
        MethodSpec.Builder builderMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        builderMethod.addParameter(ClassName.get("android.content", "Context"), "context");
        builderMethod.addParameter(ClassName.get(Integer.class), "requestCode");
        generateCommonStart(builderMethod, fieldHolders, clazzName);
        generateIsCloseActivity(fieldHolders, builderMethod);
        generateAddFlags(fieldHolders, builderMethod);
        builderMethod.addStatement(String.format("((%s)context).startActivityForResult(intent,%s)", "android.app.Activity","requestCode"));
        return builderMethod;
    }

    private void generateIsCloseActivity(List<FieldHolder> fieldHolders, MethodSpec.Builder builderMethod) {
        for (FieldHolder fieldHolder : fieldHolders) {
            String fieldType = fieldHolder.getClazz().toString();
            if ((boolean.class.getName().equals(fieldType) || Boolean.class.getName().equals(fieldType)) && fieldHolder.isCloseFromActivity()) {
                builderMethod.addCode(String.format("if(%s){", fieldHolder.getVariableName()));
                builderMethod.addStatement(String.format("\n((%s)context).finish()","android.app.Activity"));
                builderMethod.addCode("}\n");
            }
        }
    }

    private void generateAddFlags(List<FieldHolder> fieldHolders, MethodSpec.Builder builderMethod) {
        for (FieldHolder fieldHolder : fieldHolders) {
            String fieldType = fieldHolder.getClazz().toString();
            if ((int.class.getName().equals(fieldType) || Integer.class.getName().equals(fieldType)) && fieldHolder.getAddFlags()&& fieldHolder.isExclude()) {
                builderMethod.addCode(String.format("if(%s!=0){", fieldHolder.getVariableName()));
                builderMethod.addStatement(String.format("\nintent.addFlags(%s)", fieldHolder.getVariableName()));
                builderMethod.addCode("}\n");
            }
        }
    }
}
