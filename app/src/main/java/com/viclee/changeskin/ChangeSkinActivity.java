package com.viclee.changeskin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 通过生成对应插件的Resources类，实现插件换肤
 * 插件1：
 * image：微信，color：#FF4081，size：20dp，text：我来自插件1
 * 插件2：
 * image：微博，color：#00b2ee，size：30dp，text：我来自插件2
 */
public class ChangeSkinActivity extends Activity implements View.OnClickListener {
    //皮肤包资源对象
    private Resources mSkinResouces[] = new Resources[SkinConstants.SKIN_PLUGIN_NUMBER];
    //皮肤包的包名
    private String mPackageName[] = new String[SkinConstants.SKIN_PLUGIN_NUMBER];

    private ImageView imageView;
    private TextView textView;
    private Button btn1;
    private Button btn2;

    private boolean isInited = false;
    //缓存的皮肤包目录
    private String mCachedFileName1;
    private String mCachedFileName2;

    private ProgressDialog mLoadingDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }

            getPackageNames();
            mSkinResouces[0] = loadResources(mCachedFileName1);
            mSkinResouces[1] = loadResources(mCachedFileName2);

            if (mSkinResouces[0] != null && mSkinResouces[1] != null) {
                isInited = true;
                changeSkin(msg.what);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_skin);

        imageView = (ImageView) findViewById(R.id.change_skin_image);
        textView = (TextView) findViewById(R.id.change_skin_text);
        btn1 = (Button) findViewById(R.id.change_skin_btn1);
        btn1.setOnClickListener(this);
        btn2 = (Button) findViewById(R.id.change_skin_btn2);
        btn2.setOnClickListener(this);

        mCachedFileName1 = ChangeSkinActivity.this.getCacheDir() + File.separator + SkinConstants.PLUGIN_FILE_NAME1;
        mCachedFileName2 = ChangeSkinActivity.this.getCacheDir() + File.separator + SkinConstants.PLUGIN_FILE_NAME2;

        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setTitle("正在加载...");

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    /**
     * 获得对应的包名
     */
    private void getPackageNames() {
        PackageInfo mInfo1 = getPackageManager().getPackageArchiveInfo(mCachedFileName1, PackageManager.GET_ACTIVITIES);
        mPackageName[0] = mInfo1.packageName;
        PackageInfo mInfo2 = getPackageManager().getPackageArchiveInfo(mCachedFileName2, PackageManager.GET_ACTIVITIES);
        mPackageName[1] = mInfo2.packageName;
    }

    /**
     * 生成对应apk的Resources对象
     *
     * @param dexFile
     * @return
     */
    private Resources loadResources(String dexFile) {
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexFile);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Resources superRes = getResources();
        Resources skinRes = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        return skinRes;
    }

    /**
     * 使用插件中拿到的资源去更新UI
     *
     * @param i
     */
    private void changeSkin(int i) {
        int drawableId = mSkinResouces[i].getIdentifier(SkinConstants.DRAWABLE_ID, "drawable", mPackageName[i]);
        int textId = mSkinResouces[i].getIdentifier(SkinConstants.TEXT_ID, "string", mPackageName[i]);
        int textSizeId = mSkinResouces[i].getIdentifier(SkinConstants.TEXT_SIZE_ID, "dimen", mPackageName[i]);
        int textColorId = mSkinResouces[i].getIdentifier(SkinConstants.TEXT_COLOR_ID, "color", mPackageName[i]);
        imageView.setBackgroundDrawable(mSkinResouces[i].getDrawable(drawableId));
        textView.setText(mSkinResouces[i].getString(textId));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSkinResouces[i].getDimension(textSizeId));
        textView.setTextColor(mSkinResouces[i].getColor(textColorId));
    }

    /**
     * 将文件从assets中拷贝到caches目录下，方便使用文件名访问
     *
     * @param index
     */
    private void asycLoadPlugin(final int index) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyPlugin2Cache(SkinConstants.PLUGIN_FILE_NAME1);
                copyPlugin2Cache(SkinConstants.PLUGIN_FILE_NAME2);
                mHandler.obtainMessage(index).sendToTarget();
            }

            private void copyPlugin2Cache(String fileName) {
                InputStream is = null;
                FileOutputStream fo = null;
                try {
                    is = getResources().getAssets().open(fileName);
                    if (is == null) {
                        return;
                    }
                    if (SkinConstants.PLUGIN_FILE_NAME1.equals(fileName)) {
                        fo = new FileOutputStream(mCachedFileName1);
                    } else {
                        fo = new FileOutputStream(mCachedFileName2);
                    }

                    byte[] buffer = new byte[4 * 1024];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        fo.write(buffer, 0, len);
                        fo.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fo != null) {
                        try {
                            fo.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_skin_btn1) {
            //apk还没有拷贝到缓存下，则先异步拷贝plugin apk
            if (!isInited) {
                mLoadingDialog.show();
                asycLoadPlugin(0);
                return;
            }
            changeSkin(0);
        } else if (v.getId() == R.id.change_skin_btn2) {
            //apk还没有拷贝到缓存下，则先异步拷贝plugin apk
            if (!isInited) {
                mLoadingDialog.show();
                asycLoadPlugin(1);
                return;
            }
            changeSkin(1);
        }
    }

}
