# AutoBundleSample
# 前言
**APT(Annotation Processor Tool)**是用来处理注解的，即注解处理器。

**APT** 在编译器会扫描处理源代码中的注解，我们可以使用这些注解，然后利用 **APT** 自动生成 **Java** 代码，减少模板代码，提升编码效率，使源码更加简洁，可读性更高。

# 1、具体场景
下面我将会以项目中常见的 intent 页面跳转为例，给大家演示一下，如何自动生成 intent 代码，以及对 getIntent 的参数自动赋值。
>要实现上面这个功能我们需要了解  **APT**、以及 **JavaPoet**。如果不太了解的同学可以先去了解一下。

**常用写法**
```
Intent intent = new Intent(this,OtherActivity.class);
intent.putExtra("name",name);
intent.putExtra("gender",gender);
startActivity(intent);
```
**数据获取**
```
String name = getIntent().getStringExtra("name",name);
String gender = getIntent().getStringExtra("gender",gender);
```
上述代码很必要但重复性又很高，写多了会烦，又浪费时间。并且在数据传递与获取时 `key` 值都需要保持一致，这又需要我们新建很多的常量。所以，这里我们希望上述的数据传递与获取可以自动生成。

**为了实现这个需求，我们需要实现如下功能：**

**1）自动为 OtherActivity 类生成一个叫做 OtherActivityAutoBundle 的类**

**2）使用建造者模式为变量赋值**

**3）支持 startActivity 或 startActivityForResult 跳转**

**4）支持调用一个方法即可解析 Intent 传递的数据，并赋值给跳转的 Activity 中的变量**

**我们需要自动化如下代码：**
```
new OtherActivityAutoBundle()
        .name("小明")
        .gender("男")
        .start(this);//或 startActivityForResult(this,requestCode)
```
**在 OtherActivity 中，自动为变量赋值：**
```
new OtherActivityAutoBundle().bindIntentData(this,getIntent());
```
# 2、搭建 APT 项目
**a、创建一个 Java Library，并创建注解类**
例如：
```
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
```
**b、再创建一个 Java Library，并将上一步 Java Library 添加进来**
>此时，我们还需要在该 Library 中创建 `resources` 文件夹；接着在 `resources` 中创建 `META-INF` 和 `services` 两个文件夹；然后在 `services` 中创建一个名为 `javax.annotation.processing.Processor` 的文件。最后在该文件中写入我们注解处理器的全路径。

>这里我们也可以使用自动化工具 `implementation 'com.google.auto.service:auto-service:1.0-rc2'` 感兴趣的去搜一下具体用法

![image.png](https://upload-images.jianshu.io/upload_images/2726727-b0fec001309a78b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 3、创建自己的处理类，继承 AbstractProcessor
```
public class AutoBundleProcessor extends AbstractProcessor {
}
```
在创建 AutoBundleProcessor 后，我们需要重写几个方法
```
 @Override
 public synchronized void init(ProcessingEnvironment ev) {
 }
```
>在编译开始时首先会回调此方法，在这里，我们可以获取一些实例为后面做准备。
```
@Override
public boolean process(Set<? extends TypeElement> set, RoundEnvironment rev) {
}
```
>在该方法中，我们能够获取需要的类、变量、注解等相关信息，后面我们会利用这些来生成代码
```
@Override
public Set<String> getSupportedAnnotationTypes() {
}
```
>该方法中我们可以指定具体需要处理哪些注解

接着我们需要使用到 `Elements`、 `Filer`、`Name`、`TypeMirror` 对象

**Elements：**对 `Element` 对象进行操作

**Filer：**文件操作接口，它可以创建 Java 文件

**Name：**表示类名、方法名

**TypeMirror：**表示数据类型。如 `int`、`String`、以及自定义数据类型

下面我们可以获取被 `@AutoBundle` 注解元素的相关信息:
```
Set<? extends Element> elementsAnnotatedWith = rev.getElementsAnnotatedWith(AutoBundle.class);
for (Element element : elementsAnnotatedWith) {
     if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                //类名
                String className = typeElement.getSimpleName().toString();
                //包名
                String packageName = mElementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                AutoBundle autoBundle = variableElement.getAnnotation(AutoBundle.class);
                //变量名
                Name simpleName = variableElement.getSimpleName();
                //变量类型
                TypeMirror typeMirror = variableElement.asType();
      }
 }
```
例如：
>变量：gender、type：java.lang.String

其他变量亦是如此。

现在我们需要新建类来保存上面获取的值。这里我们新建 `FieldHolder` 来保存变量类型、变量名以及其他信息。

**FieldHolder**
```
public class FieldHolder {

    private String variableName;//变量名
    private TypeMirror clazz;//字段类型(如：String)
    private String packageName;//包名
    private boolean addFlags;//是否是添加 activity 启动方式
    private boolean exclude;//是否参与 intent、bundle 传值
    private boolean closeFromActivity;//是否关闭当前 Activity
    private boolean isBundle;//是否使用 Bundle 传值
    private boolean isSerializable;//是否实现 Serializable 接口的类
    private boolean isParcelable;//是否是自定义类实现 Parcelable 接口
    private boolean isParcelableArray;//是否是自定义类 ParcelableArray 类型
    private boolean isParcelableArrayList;//是否是自定义类 ParcelableArrayList 类型
}
```
## 4、下面我们需要使用 JavaPoet 生成 Java 文件
#### 简单介绍下需要用到的 API
**A、TypeSpec.Builder**
>主要用于生成类，这里的类包括的范围比较广，可以是一个 `class`、一个 `interface` 等等。

| 方法        | 功能           |
| ------------- |:-------------:| 
| classBuilder     | 生成类 |
| interfaceBuilder     | 生成接口      |  

**B、MethodSpec.Builder**
>主要用于生成类

| 方法        | 功能           |
| ------------- |:-------------:| 
| constructBuilder     | 生成构造方法 |
| methodBuilder     | 生成成员方法      |

**C、FieldSpec.Builder**
>主要用于生成成员变量

| 方法        | 功能           |
| ------------- |:-------------:| 
| builder     | 生成一个成员变量 |

**D、JavaFile.Builder**
>主要用来生成 Java 文件

| 方法        | 功能           |
| ------------- |:-------------:| 
| builder     | 生成一个 JavaFile 对象 |
|writeTo     | 将数据写到 Java 文件中|

**E、其他方法**
| 方法        | 功能           |描述|
| ------------- |:-------------:| :-------------:| 
| addModifier     | 添加修饰符 |比如：public、private、static 等等
| addParameter     | 添加参数|向方法中添加参数。例：addParameter(ClassName.get("包名"),"类名")
|addStatement|添加陈述|直接添加代码。例：addStatement("return this")|
|addCode|添加代码语句|直接添加代码，自动帮你导入需要的包，并在末尾自动添加分号|
|returns|添加返回值|为方法添加返回值。例：returns(void.class)|
|addMethod|添加方法|将生成的方法添加到类中。例：addMethod(customMethod.build())|
|addField|添加变量|将生成的变量添加到类中。例：addField(customField.build())|


**生成成员变量以及变量的 set 方法**
```
TypeSpec.Builder typeClass = TypeSpec.classBuilder(clazzName + "AutoBundle");
for (FieldHolder fieldHolder : fieldHolders) {
       packageName = fieldHolder.getPackageName();
       FieldSpec builder = FieldSpec.builder(ClassName.get(fieldHolder.getClazz()), fieldHolder.getVariableName(), Modifier.PRIVATE).build();
       typeClass.addField(builder);
       MethodSpec.Builder buildParamMethod = MethodSpec.methodBuilder(String.format("%s", fieldHolder.getVariableName()));
       buildParamMethod.addParameter(ClassName.get(fieldHolder.getClazz()), fieldHolder.getVariableName());
       buildParamMethod.addStatement(String.format("this.%s=%s", fieldHolder.getVariableName(), fieldHolder.getVariableName()));
       buildParamMethod.addStatement(String.format("return %s", "this"));
       buildParamMethod.addModifiers(Modifier.PUBLIC);
       buildParamMethod.returns(ClassName.get(fieldHolder.getPackageName(), clazzName + "AutoBundle"));
       typeClass.addMethod(buildParamMethod.build());
}
```
**生成的代码：**
```
public class OtherActivityAutoBundle {
  private String name;
  private String gender;
  public OtherActivityAutoBundle name(String name) {
      this.name = name;
      return this;
  }
  public OtherActivityAutoBundle gender(String gender) {
      this.gender = gender;
      return this;
  }
}
```
**生成 start 方法**
```
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
            } 
}
```
**结果**
```
public void start(Context context) {
        Intent intent = new Intent(context, OtherActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("is", is);
        intent.putExtra("mByte", mByte);
        intent.putExtra("b", b);
        intent.putExtra("mShort", mShort);
        intent.putExtra("mLong", mLong);
        intent.putExtra("mFloat", mFloat);
        intent.putExtra("mDouble", mDouble);
        context.startActivity(intent);
  }
```
**生成 bindIntentData**
```
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
                } 
}
```
**生成的结果**
```
public void bindIntentData(OtherActivity target, Intent intent) {
        target.id = intent.getIntExtra("id", 0);
        target.name = intent.getStringExtra("name");
        target.is = intent.getBooleanExtra("is", false);
        target.mByte = intent.getByteExtra("mByte", (byte) 0);
        target.b = intent.getCharExtra("b", (char) 0);
        target.mShort = intent.getShortExtra("mShort", (short) 0);
        target.mLong = intent.getLongExtra("mLong", 0);
        target.mFloat = intent.getFloatExtra("mFloat", 0);
        target.mDouble = intent.getDoubleExtra("mDouble", 0);
}
```
**最后将生成好的 Java 代码写入文件**
```
 //与目标 Class 放在同一个包下，解决 Class 属性的可访问性
 JavaFile javaFile = JavaFile.builder(packageName, typeClass.build())
           .build();
  try {
         //生成 class 文件
         javaFile.writeTo(mFiler);
       } catch (IOException e) {
           e.printStackTrace();
     }
```
生成的文件在 `app/build/generated/ap_generated_sources/debug/out/包名/xxx`
![image.png](https://upload-images.jianshu.io/upload_images/2726727-902796eb5caf5db2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**最后生成的代码：**
```
/**
 * This codes are generated automatically. Do not modify! */
public class ThirdActivityAutoBundle {
  private int no;

  private String address;

  private boolean isChoose;

  public ThirdActivityAutoBundle no(int no) {
    this.no=no;
    return this;
  }

  public ThirdActivityAutoBundle address(String address) {
    this.address=address;
    return this;
  }

  public ThirdActivityAutoBundle isChoose(boolean isChoose) {
    this.isChoose=isChoose;
    return this;
  }

  public void start(Context context) {
    Intent intent = new Intent(context,ThirdActivity.class);
    intent.putExtra("no",no);
    intent.putExtra("address",address);
    intent.putExtra("isChoose",isChoose);
    context.startActivity(intent);
  }

  public void startForResult(Context context, Integer requestCode) {
    Intent intent = new Intent(context,ThirdActivity.class);
    intent.putExtra("no",no);
    intent.putExtra("address",address);
    intent.putExtra("isChoose",isChoose);
    ((android.app.Activity)context).startActivityForResult(intent,requestCode);
  }

  public void bindIntentData(ThirdActivity target, Intent intent) {
    target.no=intent.getIntExtra("no",0);
    target.address=intent.getStringExtra("address");
    target.isChoose = intent.getBooleanExtra("isChoose",false);
  }
}

```
好了，到这里就全部结束了，大家可以发挥想象力添加自己想要的功能。
