package com.alitajs.micro.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alitajs.micro.AlitaOtherAgent;
import com.alitajs.micro.R;
import com.alitajs.micro.bean.ThemeBean;
import com.alitajs.micro.data.ConstantValue;
import com.alitajs.micro.event.NoticeEvent;
import com.alitajs.micro.ui.bridge.DeviceAlitaBridge;
import com.alitajs.micro.ui.bridge.FileAlitaBridge;
import com.alitajs.micro.ui.bridge.LocationAlitaBridge;
import com.alitajs.micro.ui.bridge.MediaAlitaBridge;
import com.alitajs.micro.ui.bridge.NoticeAlitaBridge;
import com.alitajs.micro.ui.bridge.UIAlitaBridge;
import com.alitajs.micro.ui.dialog.LoadingDialog;
import com.alitajs.micro.ui.web.AlitaNativeWebView;
import com.alitajs.micro.ui.web.MicroWebChromeClient;
import com.alitajs.micro.utils.FileUtil;
import com.alitajs.micro.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MicroAppOtherActivity extends BaseMiniActivity implements MicroWebChromeClient.OpenFileChooserCallBack {

    static final int REQUEST_CODE_PICK_IMAGE = 10;

    RelativeLayout mNarBar;
    RelativeLayout mNarBarBack;
    RelativeLayout mFlParent;
    FrameLayout mContentView;
    TextView mNarBarTitle;
    AppCompatImageView mNarBackIcon;
    AppCompatImageView mNarCloseIcon;
    AlitaNativeWebView mWebView;
    LoadingDialog mLoadingDialog;

    UIAlitaBridge uiAlitaBridge;
    DeviceAlitaBridge deviceAlitaBridge;
    MediaAlitaBridge mediaAlitaBridge;
    FileAlitaBridge fileAlitaBridge;
    NoticeAlitaBridge noticeAlitaBridge;
    LocationAlitaBridge locationAlitaBridge;

    ThemeBean mThemeBean;

    String htmlPath;
    String mUserData;
    String mUrl;
    boolean isNeedTopbar = true;
    String mCurUrl;

    ValueCallback<Uri> mUploadMsg;
    ValueCallback<Uri[]> mUploadMsg5Plus;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                //TODO 回调处理
                switch (msg.what) {
                    case ConstantValue.MESSAGE_TYPE_NARBAR:
                        JSONObject jsonObject = new JSONObject(String.valueOf(msg.obj));
                        if (jsonObject.has("backgroundColor")) {
                            mNarBar.setBackgroundColor(Color.parseColor(jsonObject.optString("backgroundColor")));
                        }
                        if (jsonObject.has("color")) {
                            mNarBarTitle.setTextColor(Color.parseColor(jsonObject.optString("color")));
                            VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.close, getTheme());
                            //你需要改变的颜色
                            vectorDrawableCompat.setTint(Color.parseColor(jsonObject.optString("color")));
                            mNarCloseIcon.setImageDrawable(vectorDrawableCompat);

                            VectorDrawableCompat back = VectorDrawableCompat.create(getResources(), R.drawable.back_icon, getTheme());
                            //你需要改变的颜色
                            back.setTint(Color.parseColor(jsonObject.optString("color")));
                            mNarBackIcon.setImageDrawable(back);
                            //深色
                            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            //浅色
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                        }
                        if (jsonObject.has("fontSize")) {
                            mNarBarTitle.setTextSize(jsonObject.optInt("fontSize"));
                        }

                        break;
                    case ConstantValue.MESSAGE_TYPE_NARBAR_TITLE:
                        mNarBarTitle.setText(String.valueOf(msg.obj));
                        break;
                    case ConstantValue.MESSAGE_TYPE_NARBAR_BACK_EVENT:
                        //mWebView.loadUrl("javascript:alita.fireDocumentEvent(\"back\")");
                        onBackClick();
                        break;
                    case ConstantValue.MESSAGE_TYPE_NARBAR_BACK:
                        if (mWebView.canGoBack())
                            mWebView.goBack();
                        break;
                    case ConstantValue.MESSAGE_TYPE_NARBAR_CLOSE:
                        finish();
                        break;
                    case ConstantValue.MESSAGE_TYPE_NARBAR_BACK_VISIBLE:
                        //显示/隐藏 返回按钮
                        //mNarBarBack.setVisibility();
                        break;
                    case ConstantValue.MESSAGE_TYPE_WEBVIEW_BACKGROUND:
                        JSONObject jb = new JSONObject(String.valueOf(msg.obj));
                        if (jb.has("backgroundColor")) {
                            mWebView.setBackgroundColor(Color.parseColor(jb.optString("backgroundColor")));
                        }
                        break;
                    case ConstantValue.MESSAGE_TYPE_STATUSBAR:
                        JSONObject status = new JSONObject(String.valueOf(msg.obj));
                        if (status.has("theme")) {
                            String theme = status.optString("theme");
                            if (theme.equals("light")) {
                                //浅色
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                            } else if (theme.equals("dark")) {
                                //深色
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void getExtraDatas() {
        super.getExtraDatas();
        if (getIntent() != null) {
            htmlPath = getIntent().getStringExtra("htmlPath");
            mUserData = getIntent().getStringExtra("userData");
            mUrl = getIntent().getStringExtra("url");
            isNeedTopbar = getIntent().getBooleanExtra("needTopbar", true);
            mThemeBean = (ThemeBean) getIntent().getSerializableExtra("theme");
            Log.i("caicai", "getExtraDatas mUserData = " + mUserData);
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_microapp;
    }

    @Override
    protected void init() {
        LogUtil.i("caicai", "init");
        initLoadingDialog();
        mWebView = AlitaOtherAgent.getWebView();
        mWebView.setBackgroundResource(R.color.transparent);
        uiAlitaBridge = new UIAlitaBridge(MicroAppOtherActivity.this);
        uiAlitaBridge.setHandler(mHandler);
        mWebView.addJavascriptObject(uiAlitaBridge, "ui");
        deviceAlitaBridge = new DeviceAlitaBridge(MicroAppOtherActivity.this);
        deviceAlitaBridge.setHandler(mHandler);
        mWebView.addJavascriptObject(deviceAlitaBridge, "device");//Window.Android.xxx()
        mediaAlitaBridge = new MediaAlitaBridge(MicroAppOtherActivity.this);
        mWebView.addJavascriptObject(mediaAlitaBridge, "media");
        fileAlitaBridge = new FileAlitaBridge(MicroAppOtherActivity.this);
        mWebView.addJavascriptObject(fileAlitaBridge, "file");
        locationAlitaBridge = new LocationAlitaBridge(MicroAppOtherActivity.this);
        mWebView.addJavascriptObject(locationAlitaBridge, "location");
        noticeAlitaBridge = new NoticeAlitaBridge(MicroAppOtherActivity.this);
        mWebView.addJavascriptObject(noticeAlitaBridge, "notice");
        mWebView.setWebChromeClient(new MicroWebChromeClient(mActivity, this));
        try {
            if (!TextUtils.isEmpty(mUserData)) {
                deviceAlitaBridge.setUserData(new JSONObject(mUserData));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void setViews() {
        mNarBar = findViewById(R.id.navbar);
        mNarBarBack = findViewById(R.id.navbar_back);
        mNarBarTitle = findViewById(R.id.navbar_title);
        mFlParent = findViewById(R.id.fl_parent);
        mContentView = findViewById(R.id.content);
        mNarCloseIcon = findViewById(R.id.close);
        mNarBackIcon = findViewById(R.id.navbar_back_icon);
        mNarBar.setVisibility(isNeedTopbar ? View.VISIBLE : View.GONE);
        ViewGroup parent = (ViewGroup) mWebView.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            parent.removeView(mWebView);
        }
        mContentView.addView(mWebView);

        if (!TextUtils.isEmpty(htmlPath)) {
            //TODO 获取asset-manifest.json下的配置
            String file = htmlPath + "/asset-manifest.json";
            String json = FileUtil.readTxtFile(file.replace("file:///", ""));
            try {
                //解析获取标题
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has("name")) {
                    String name = jsonObject.optString("name");
                    mNarBarTitle.setText(name);
                }
                if (jsonObject.has("navBar")) {
                    JSONObject navBar = jsonObject.optJSONObject("navBar");
                    if (navBar.has("backgroundColor")) {
                        mNarBar.setBackgroundColor(Color.parseColor(navBar.optString("backgroundColor")));
                    }
                    if (navBar.has("color")) {
                        mNarBarTitle.setTextColor(Color.parseColor(navBar.optString("color")));
                        VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.close, getTheme());
                        //你需要改变的颜色
                        vectorDrawableCompat.setTint(Color.parseColor(navBar.optString("color")));
                        mNarCloseIcon.setImageDrawable(vectorDrawableCompat);
                    }
                    if (navBar.has("fontSize")) {
                        mNarBarTitle.setTextSize(navBar.optInt("fontSize"));
                    }
                }
                if (jsonObject.has("backgroundColor")) {
                    String backgroundColor = jsonObject.optString("backgroundColor");
                    mWebView.setBackgroundColor(Color.parseColor(backgroundColor));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //TODO 额外开的主题口 设置头部
        if (mThemeBean != null) {
            try {
                if (!TextUtils.isEmpty(mThemeBean.getBackgroundColor())) {
                    mNarBar.setBackgroundColor(Color.parseColor(mThemeBean.getBackgroundColor()));
                }
                if (!TextUtils.isEmpty(mThemeBean.getTextColor())) {
                    mNarBarTitle.setTextColor(Color.parseColor(mThemeBean.getTextColor()));
                    VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.close, getTheme());
                    //你需要改变的颜色
                    vectorDrawableCompat.setTint(Color.parseColor(mThemeBean.getTextColor()));
                    mNarCloseIcon.setImageDrawable(vectorDrawableCompat);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void setListeners() {
        findViewById(R.id.navbar_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                mWebView.setBackgroundColor(Color.parseColor("#ffffff"));
                mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                mWebView.clearHistory();
            }
        });

        mNarBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackClick();
            }
        });

        AlitaOtherAgent.setPageStateListener(new AlitaOtherAgent.PageStateListener() {
            @Override
            public void onPageStarted(String url) {
                mCurUrl = url;
            }

            @Override
            public void onPageFinished(String url) {
                if (mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                    mWebView.clearHistory();
                }
                mNarBarBack.setVisibility(mWebView.canGoBack() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void onBackClick() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            mNarBarBack.setVisibility(mWebView.canGoBack() ? View.VISIBLE : View.GONE);
            return;
        }
        finish();
        mWebView.setBackgroundColor(Color.parseColor("#ffffff"));
        mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        mWebView.clearHistory();
    }

    private void initLoadingDialog() {
        mLoadingDialog = new LoadingDialog(mActivity);
        mLoadingDialog.show();
    }

    @Override
    protected void release() {
        super.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mediaAlitaBridge != null) {
            mediaAlitaBridge.onActivityResult(requestCode, resultCode, data);
        }
        if (deviceAlitaBridge != null) {
            deviceAlitaBridge.onActivityResult(requestCode, resultCode, data);
        }

        if (locationAlitaBridge != null) {
            locationAlitaBridge.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == RESULT_CANCELED) {
            Uri result = Uri.EMPTY;
            sendPhoto(result);
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (data != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                sendPhoto(data.getData());
            } else {
                String imagePath = getImagePath(data.getData(), null);
                sendPhoto(Uri.fromFile(new File(imagePath)));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO 调用js通知
        mWebView.loadUrl("javascript:WebViewJavascriptBridge.fireDocumentEvent(\"resume\")");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO 调用js通知
        mWebView.loadUrl("javascript:WebViewJavascriptBridge.fireDocumentEvent(\"pause\")");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // mWebView.loadUrl("javascript:WebViewJavascriptBridge.fireDocumentEvent(\"back\")");
        onBackClick();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //mWebView.loadUrl("javascript:WebViewJavascriptBridge.fireDocumentEvent(\"back\")");
            onBackClick();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(NoticeEvent message) {
        if (message.getName().equals("finish_microapp")) {
            finish();
            mWebView.setBackgroundColor(Color.parseColor("#ffffff"));
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void showOptions() {
        requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, new OnRequestPermissionListen() {
            @Override
            public void succeed() {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }

            @Override
            public void fail() {
                Toast.makeText(mActivity, "请开启存储权限后操作", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //发送图片
    public void sendPhoto(Uri uri) {
        if (mUploadMsg != null) {
            mUploadMsg.onReceiveValue(uri);
            mUploadMsg = null;
        } else {
            mUploadMsg5Plus.onReceiveValue(new Uri[]{uri});
            mUploadMsg5Plus = null;
        }

    }

    /**
     * 根据uri获取图片地址
     **/
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection老获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public void openFileChooserCallBack(com.tencent.smtt.sdk.ValueCallback<Uri> uploadMsg, String acceptType) {
        mUploadMsg = uploadMsg;
        showOptions();
    }

    @Override
    public void showFileChooserCallBack(com.tencent.smtt.sdk.ValueCallback<Uri[]> filePathCallback) {
        mUploadMsg5Plus = filePathCallback;
        showOptions();
    }
}
