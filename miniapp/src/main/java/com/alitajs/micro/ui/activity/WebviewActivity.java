package com.alitajs.micro.ui.activity;

import android.graphics.Bitmap;
import android.view.View;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.alitajs.micro.R;
import com.alitajs.micro.utils.LogUtil;

public class WebviewActivity extends BaseMiniActivity {

    public static final String EXTRA_URL = "EXTRA_URL";

    protected WebView mWebView;
    protected String url;

    @Override
    protected void getExtraDatas() {
        super.getExtraDatas();
        if (getIntent() != null) {
            url = getIntent().getStringExtra(EXTRA_URL);
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_webview;
    }

    @Override
    protected void init() {
        LogUtil.i("caicai","init");
    }

    @Override
    protected void setViews() {
        mWebView = findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        settings.setSavePassword(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        settings.setSupportMultipleWindows(false);
        settings.setBlockNetworkImage(false);
        settings.setBlockNetworkLoads(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setLoadsImagesAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        String appCachePath = getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAppCacheEnabled(true);    //开启H5(APPCache)缓存功能
        settings.setAllowFileAccess(true);// 可以读取文件缓存
        settings.setAllowContentAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        /*WebActivity.BaseJavaScript javaScript = new WebActivity.BaseJavaScript();
        mWebView.addJavascriptInterface(javaScript, "Android");*/
        addJavascriptInterface();
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogUtil.i("caicai","onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogUtil.i("caicai","onPageFinished");
            }

        });
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.requestFocus();
        mWebView.loadUrl(url);
    }

    @Override
    protected void setListeners() {

    }

    protected void addJavascriptInterface(){

    }

}
