# Android-Plugin-Change-Skin

       Android App实现换肤有很多方式，有的是通过内置资源的方式，有的是通过设置相同签名并且AndroidManifest.xml中配置相同android:sharedUserId使得两个apk运行在同一个进程中来互相访问数据。但是这些方式都有其局限性，实现不够灵活。今天来聊一下通过插件的方式换肤的原理及实现。
       这种实现的思路是这样的：
1）创建宿主工程，生成对应的UI页面，确定哪些资源需要动态替换。
2）创建单独的Android子工程，将需要替换的资源（color、string、image等）打包进apk，但是要保证资源的名字和主工程中的完全相同。
3）在宿主工程生成对应于插件资源的Resources对象，通过Resouces对象加载插件apk的资源，从而实现换肤的目的。
       首先来认识一下Resources类，它是App中资源的管理类。一个App中的某一个固定配置（可以理解为在一个手机上）只会对应于一个Resources。程序运行时可以通过Context的getResources()方法得到当前应用的Resouces对象，通过Resouces对象就可以访问res目录下的资源。如果需要访问App之外的资源（如插件apk），则需要创建对应于当前apk资源的Resources对象。
       那么，这里的关键就是怎么得到Resouces对象。
		实现插件换肤的关键就是这样一段代码：

	try {
		AssetManager assetManager = AssetManager.class.newInstance();
		Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
		addAssetPath.invoke(assetManager, mDexPath);
		mAssetManager = assetManager;
	} catch (Exception e) {
		e.printStackTrace();
	}
	Resources currentRes = this.getResources();
	mResources = new Resources(mAssetManager, currentRes.getDisplayMetrics(),
			currentRes.getConfiguration());


       Resources类的构造函数需要三个参数，其中最重要的就是第一个参数AssetManager，其他两个都是和设备相关的参数，直接用当前应用的配置就好了。AssetManager需要调用addAssetPath(resDir)方法是把资源目录中的资源加载到AssetManager对象中，由于这个方法在sdk中是hide的，只能通过反射来调用。有了Resources对象，我们就可以通过它来访问插件apk里面的各种资源了。

具体的效果如下：

![](https://github.com/viclee2014/Android-Plugin-Change-Skin/blob/master/app/src/main/res/raw/change_skin.gif)
