package com.alitajs.micro.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alitajs.micro.AlitaAgent;
import com.alitajs.micro.R;
import com.alitajs.micro.data.ConstantValue;
import com.alitajs.micro.ui.bridge.DeviceAlitaBridge;
import com.alitajs.micro.ui.bridge.FileAlitaBridge;
import com.alitajs.micro.ui.bridge.LocationAlitaBridge;
import com.alitajs.micro.ui.bridge.MediaAlitaBridge;
import com.alitajs.micro.ui.bridge.UIAlitaBridge;
import com.alitajs.micro.ui.web.AlitaNativeWebView;
import com.alitajs.micro.utils.FileUtil;
import com.alitajs.micro.utils.LogUtil;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MicroAppNativeActivity extends BaseMiniActivity {

    RelativeLayout mNarBar;
    RelativeLayout mNarBarBack;
    RelativeLayout mFlParent;
    TextView mNarBarTitle;
    AppCompatImageView mNarCloseIcon;
    AlitaNativeWebView mWebView;

    UIAlitaBridge uiAlitaBridge;
    DeviceAlitaBridge deviceAlitaBridge;
    MediaAlitaBridge mediaAlitaBridge;
    FileAlitaBridge fileAlitaBridge;
    LocationAlitaBridge locationAlitaBridge;

    String htmlPath;
    String mUserData;
    boolean isNeedTopbar = true;

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
            isNeedTopbar = getIntent().getBooleanExtra("needTopbar", true);
            Log.i("caicai", "getExtraDatas mUserData = " + mUserData);
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_microapp_native;
    }

    @Override
    protected void init() {
        LogUtil.i("caicai", "init");
    }

    @Override
    protected void setViews() {
        mNarBar = findViewById(R.id.navbar);
        mNarBarBack = findViewById(R.id.navbar_back);
        mNarBarTitle = findViewById(R.id.navbar_title);
        mFlParent = findViewById(R.id.fl_parent);
        mContentView = findViewById(R.id.content);
        mNarCloseIcon = findViewById(R.id.close);
        mWebView = findViewById(R.id.webview);
        mNarBar.setVisibility(isNeedTopbar ? View.VISIBLE : View.GONE);
        ViewGroup parent = (ViewGroup) mWebView.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            parent.removeView(mWebView);
        }

        final String vendors = FileUtil.getJsStr(mActivity, "web-framework.js");
        final String jsStr = FileUtil.getJsStr(mActivity, "dsbridge.js");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtil.i("caicai", "shouldOverrideUrlLoading " + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogUtil.i("caicai", "onPageStarted " + url);
                mWebView.evaluateJavascript(vendors, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("caicai", "vendors " + value);
                    }
                });
                mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("caicai", "jsStr " + value);
                    }
                });
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogUtil.i("caicai", "onPageFinished");
                mNarBarBack.setVisibility(mWebView.canGoBack() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.i("caicai", "onLoadResource");
                mWebView.evaluateJavascript(vendors, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("caicai", "vendors " + value);
                    }
                });
                mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("caicai", "jsStr " + value);
                    }
                });
            }

            @Override
            public void doUpdateVisitedHistory(WebView webView, String s, boolean b) {
                super.doUpdateVisitedHistory(webView, s, b);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {
                return super.shouldInterceptRequest(webView, url);
            }
        });
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.requestFocus();
        mWebView.setBackgroundResource(R.color.transparent);
        uiAlitaBridge = new UIAlitaBridge(MicroAppNativeActivity.this);
        uiAlitaBridge.setHandler(mHandler);
        mWebView.addJavascriptObject(uiAlitaBridge, "ui");
        deviceAlitaBridge = new DeviceAlitaBridge(MicroAppNativeActivity.this);
        deviceAlitaBridge.setHandler(mHandler);
        mWebView.addJavascriptObject(deviceAlitaBridge, "device");//Window.Android.xxx()
        mediaAlitaBridge = new MediaAlitaBridge(MicroAppNativeActivity.this);
        mWebView.addJavascriptObject(mediaAlitaBridge, "media");
        fileAlitaBridge = new FileAlitaBridge(MicroAppNativeActivity.this);
        mWebView.addJavascriptObject(fileAlitaBridge, "file");
        locationAlitaBridge = new LocationAlitaBridge(MicroAppNativeActivity.this);
        mWebView.addJavascriptObject(locationAlitaBridge, "location");
        try {
            deviceAlitaBridge.setUserData(new JSONObject(mUserData));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(htmlPath)) {
            //TODO 获取asset-manifest.json下的配置
            /*String file = htmlPath + "/asset-manifest.json";
            String json = FileUtil.readTxtFile(file.replace("file:///", ""));
            try {
                //解析获取标题
                JSONObject jsonObject = new JSONObject(json);
                String name = jsonObject.optString("name");
                mNarBarTitle.setText(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            mWebView.loadUrl(htmlPath);
        }
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

        AlitaAgent.setPageStateListener(new AlitaAgent.PageStateListener() {
            @Override
            public void onPageStarted(String url) {

            }

            @Override
            public void onPageFinished(String url) {
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
        if (mediaAlitaBridge != null) {
            mediaAlitaBridge.onActivityResult(requestCode, resultCode, data);
        }
        if (deviceAlitaBridge != null) {
            deviceAlitaBridge.onActivityResult(requestCode, resultCode, data);
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
