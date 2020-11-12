package com.whalecloud.minialita.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whalecloud.minialita.R;
import com.whalecloud.minialita.MiniAppAgent;
import com.whalecloud.minialita.data.ConstantValue;
import com.whalecloud.minialita.ui.bridge.DeviceAlitaBridge;
import com.whalecloud.minialita.ui.bridge.FileAlitaBridge;
import com.whalecloud.minialita.ui.bridge.LocationAlitaBridge;
import com.whalecloud.minialita.ui.bridge.MediaAlitaBridge;
import com.whalecloud.minialita.ui.web.AlitaNativeWebView;
import com.whalecloud.minialita.utils.FileUtil;
import com.whalecloud.minialita.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MiniAppActivity extends BaseMiniActivity {

    RelativeLayout mNarBar;
    RelativeLayout mNarBarBack;
    RelativeLayout mFlParent;
    FrameLayout mContentView;
    TextView mNarBarTitle;
    AppCompatImageView mNarCloseIcon;
    AlitaNativeWebView mWebView;

    DeviceAlitaBridge deviceAlitaBridge;
    MediaAlitaBridge mediaAlitaBridge;
    FileAlitaBridge fileAlitaBridge;
    LocationAlitaBridge locationAlitaBridge;

    String htmlPath;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //TODO 回调处理
            switch (msg.what) {
                case ConstantValue.MESSAGE_TYPE_NARBAR:
                    try {
                        JSONObject jsonObject = new JSONObject(String.valueOf(msg.obj));
                        if (jsonObject.has("backgroundColor")){
                            mNarBar.setBackgroundColor(Color.parseColor(jsonObject.optString("backgroundColor")));
                        }
                        if (jsonObject.has("color")){
                            mNarBarTitle.setTextColor(Color.parseColor(jsonObject.optString("color")));
                            VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(),R.drawable.close,getTheme());
                            //你需要改变的颜色
                            vectorDrawableCompat.setTint(Color.parseColor(jsonObject.optString("color")));
                            mNarCloseIcon.setImageDrawable(vectorDrawableCompat);
                        }
                        if (jsonObject.has("fontSize")){
                            mNarBarTitle.setTextSize(jsonObject.optInt("fontSize"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            }
        }
    };

    @Override
    protected void getExtraDatas() {
        super.getExtraDatas();
        if (getIntent() != null) {
            htmlPath = getIntent().getStringExtra("htmlPath");
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_miniapp;
    }

    @Override
    protected void init() {
        LogUtil.i("caicai", "init");
        mWebView = MiniAppAgent.getWebView();
        deviceAlitaBridge = new DeviceAlitaBridge(MiniAppActivity.this);
        deviceAlitaBridge.setHandler(mHandler);
        mWebView.addJavascriptObject(deviceAlitaBridge, "device");//Window.Android.xxx()
        mediaAlitaBridge = new MediaAlitaBridge(MiniAppActivity.this);
        mWebView.addJavascriptObject(mediaAlitaBridge, "media");
        fileAlitaBridge = new FileAlitaBridge(MiniAppActivity.this);
        mWebView.addJavascriptObject(fileAlitaBridge, "file");
        locationAlitaBridge = new LocationAlitaBridge(MiniAppActivity.this);
        mWebView.addJavascriptObject(locationAlitaBridge, "location");
    }

    @Override
    protected void setViews() {
        mNarBar = findViewById(R.id.navbar);
        mNarBarBack = findViewById(R.id.navbar_back);
        mNarBarTitle = findViewById(R.id.navbar_title);
        mFlParent = findViewById(R.id.fl_parent);
        mContentView = findViewById(R.id.content);
        ViewGroup parent = (ViewGroup) mWebView.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            parent.removeView(mWebView);
        }
        mContentView.addView(mWebView);

        if (!TextUtils.isEmpty(htmlPath)){
            //TODO 获取asset-manifest.json下的配置
            String file = htmlPath + "/asset-manifest.json";
            String json = FileUtil.readTxtFile(file.replace("file:///",""));
            try {
                //解析获取标题
                JSONObject jsonObject = new JSONObject(json);
                String name = jsonObject.optString("name");
                mNarBarTitle.setText(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        MiniAppAgent.setNeedClearHistory(true);
    }

    @Override
    protected void setListeners() {
        findViewById(R.id.navbar_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNarBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackClick();
            }
        });

        MiniAppAgent.setPageStateListener(new MiniAppAgent.PageStateListener() {
            @Override
            public void onPageStarted(String url) {

            }

            @Override
            public void onPageFinished(String url) {
                mNarBarBack.setVisibility(mWebView.canGoBack() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void onBackClick(){
        if (mWebView.canGoBack()){
            mWebView.goBack();
            mNarBarBack.setVisibility(mWebView.canGoBack() ? View.VISIBLE : View.GONE);
            return;
        }
        finish();
    }

    @Override
    protected void release() {
        super.release();
     /*   mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        mWebView.clearHistory();
        ((ViewGroup) mWebView.getParent()).removeView(mWebView);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mediaAlitaBridge != null){
            mediaAlitaBridge.onActivityResult(requestCode, resultCode, data);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
