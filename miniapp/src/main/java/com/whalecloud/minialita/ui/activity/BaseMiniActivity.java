package com.whalecloud.minialita.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import java.util.ArrayList;

/**
 * 基础Activity 类.
 *
 */
public abstract class BaseMiniActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 10000; //权限请求code
    private OnRequestPermissionListen mOnRequestPermissionListen;

    /** Activity 实例. */
    protected AppCompatActivity mActivity;

    /** ButterKnife绑定视图 */
    protected View mContentView;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenOrientation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                View decorView = getWindow().getDecorView();
                if(decorView != null){   //白色背景要设置暗色系的状态栏图标
                    int vis = decorView.getSystemUiVisibility();
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(vis);
                }
            }
        }
        mActivity = this;
        if (provideContentViewId()!= 0){
            mContentView = this.getLayoutInflater().inflate(provideContentViewId(), null);
            setContentView(mContentView);
        }

        getExtraDatas();
        init();
        initToolbar();
        setViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    protected void setImmerseLayout(View view) {// view为标题栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(this.getBaseContext());
            view.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    protected int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void setScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            try {
                Intent intent = new Intent();
                intent.setAction("com.action.restore");
                sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取 Extra 数据.
     */
    protected void getExtraDatas() {
    }

    /**
     * 初始化Toolbar.
     */
    protected void initToolbar() {
    }

    /**
     * 布局文件layout的id.
     *
     * @return layout 对应的id.
     */
    protected abstract int provideContentViewId();

    /**
     * 初始化操作.
     */
    protected abstract void init();

    /**
     * 视图的设置操作.
     */
    protected abstract void setViews();

    /**
     * 涉及到监听器的设置操作.
     */
    protected abstract void setListeners();

    /**
     * 释放相关操作.
     */
    protected void release(){};


    /**
     * 请求权限
     * @param permissions
     * @param onRequestPermissionListen
     */
    public void requestPermission(final String[] permissions, OnRequestPermissionListen onRequestPermissionListen) {

        this.mOnRequestPermissionListen = onRequestPermissionListen;

        //收集未授权或者拒绝过的权限
        ArrayList<String> deniedPermissionList = new ArrayList<>();
        for (String per : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(this, per);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                deniedPermissionList.add(per);
            }
        }
        if (deniedPermissionList.isEmpty()) {
            // do something
            if (mOnRequestPermissionListen != null) {
                mOnRequestPermissionListen.succeed();
            }
        } else {
            String[] permissionArray = deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
            ActivityCompat.requestPermissions(this,permissionArray, PERMISSION_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PERMISSION_REQUEST_CODE == requestCode) {

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        showTipsDialog(permissions);
                    }else{
                        showSettingDialog();
                    }
                    return;
                }
            }

            if (mOnRequestPermissionListen != null) {
                mOnRequestPermissionListen.succeed();
            }
        }
    }
    /**
     * 显示提示对话框
     */
    private void showTipsDialog(final String[] permissions) {
        new AlertDialog.Builder(this)
                .setTitle("提示信息")
                .setMessage("再次提醒设置")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnRequestPermissionListen != null) {
                            mOnRequestPermissionListen.fail();
                        }
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        startAppSettings();
                        requestPermissions(permissions,PERMISSION_REQUEST_CODE );
                    }
                }).show();
    }

    private void showSettingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示信息")
                .setMessage("当前应用缺少必要权限，该功能暂时无法使用。如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnRequestPermissionListen != null) {
                            mOnRequestPermissionListen.fail();
                        }
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();

                    }
                }).show();
    }


    /**
     * 启动当前应用设置页面
     */

    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public interface OnRequestPermissionListen {
        void succeed();

        void fail();
    }

}
