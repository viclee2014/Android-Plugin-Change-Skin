# Android-Plugin-Change-Skin

Android App实现换肤有很多方式，有的是通过内置资源的方式，有的是通过设置相同签名并且AndroidManifest.xml中配置相同android:sharedUserId使得两个apk运行在同一个进程中来互相访问数据。但是这些方式都有其局限性，实现不够灵活。这里是通过插件的方式换肤的原理及实现。

插件换肤的实现思路：

1）创建宿主工程，生成对应的UI页面，确定哪些资源需要动态替换。

2）创建单独的Android子工程，将需要替换的资源（color、string、image等）打包进apk，但是要保证资源的名字和主工程中的完全相同。

3）在宿主工程生成对应于插件资源的Resources对象，通过Resouces对象加载插件apk的资源，从而实现换肤的目的。

具体分析见博客：http://blog.csdn.net/goodlixueyong/article/details/51089674


具体的效果如下：

![](https://github.com/viclee2014/Android-Plugin-Change-Skin/blob/master/app/src/main/res/raw/change_skin.gif)
