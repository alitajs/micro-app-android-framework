package com.whalecloud.minialita.ui.activity;

import android.graphics.Bitmap;
import android.view.View;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.whalecloud.minialita.R;
import com.whalecloud.minialita.utils.LogUtil;

public class WebviewActivity extends BaseMiniActivity {

    WebView mWebView;
    String htmlPath;

    @Override
    protected void getExtraDatas() {
        super.getExtraDatas();
        if (getIntent() != null) {
            htmlPath = getIntent().getStringExtra("htmlPath");
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
        mWebView.loadUrl(htmlPath);
    }

    @Override
    protected void setListeners() {

    }
}
