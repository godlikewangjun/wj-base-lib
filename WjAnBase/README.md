# WjAnBase
android开发的基础框架 。私用框架，不提供demo。

***https://jitpack.io/ 的gradle 依赖***<br>
版本：[![](https://jitpack.io/v/godlikewangjun/Wjktlib.svg)](https://jitpack.io/#godlikewangjun/Wjktlib)
```
 api 'com.github.godlikewangjun.Wjktlib:WjAnBase:1.6.9.5'
```
***项目简述***<br>
项目主要有网络请求okhttp的封装,Glide的图片缓存,一些工具类和其他UI效果等，框架还在整理,
上班没有时间也没有精力写demo，陆陆续续再更新，有时间的话会开源一些其他的项目。

***关于根build.gradle***<br>
只传了主要的源码部分，这部分的我就贴出来
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.3'
        classpath 'com.github.dcendents:android-maven-gradle-plugin:1.5'
        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.5'
    }

}

allprojects {
    repositories {
        jcenter()
    }
    tasks.withType(Javadoc) {
        options.addStringOption('Xdoclint:none', '-quiet')
        options.addStringOption('encoding', 'UTF-8')
    }

}
ext {
    compileSdkVersion = 25
    buildToolsVersion = "23.0.3"
    targetSdkVersion = 25
    minSdkVersion = 14
}
```
***项目结构和内容介绍***<br>
 okhttp包主要是okhttp的封装，用法很简单。
```
 OhHttpClient httpClient=OhHttpClient.getInit();
 httpClient.setJsonFromMat(true);//设置格式化返回的json字符串

 Builder headers=new Builder();
 headers.add("Client-Type", "android")
 httpClient.setHeaders(headers.build());//添加headr
```
 当然还有其他的配置请看源码.<br>
 请求类型直接httpClient.get() httpClient.post()之类，根据提示写就行了。
*2.1.2
精简项目,新增加密库conceal的封装
 ConcealHelper.init(this)
 System.out.println(ConcealHelper.encryptString("00-------=ijjk"));
 System.out.println(ConcealHelper.decryptString("00-------=ijjk"));
 System.out.println(ConcealHelper.decryptString(ConcealHelper.encryptString("00-------=ijjk"))+"====");
*2.1.0
修改了部分bug 新增AndroidKeyboardHeight 解决去掉状态栏和键盘弹出冲突问题，键盘弹出错误

*以下修改的部分并不影响原框架的使用
<br>**CirclePageIndicator是修改网上的源码添加了可以设置Bitmap图片的功能,不再是修改颜色。**
<br>**flowingdrawer 修改了部分bug[详情](http://blog.csdn.net/u010523832/article/details/51586125)**
<br>**observablescrollview 修改了嵌套ScrollView之流的滑动冲突问题[详情](http://blog.csdn.net/u010523832/article/details/52709144)**


**关于里面源码的修改是使用,注明出处就可以了，还是欢迎随手点个start，欢迎issues**
