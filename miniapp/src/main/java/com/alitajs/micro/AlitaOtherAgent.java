package com.alitajs.micro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.alitajs.micro.ui.web.AlitaNativeWebView;
import com.alitajs.micro.utils.FileUtil;
import com.alitajs.micro.utils.LogUtil;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AlitaOtherAgent {

    static AlitaNativeWebView mWebView;
    static boolean needClearHistory;

    static PageStateListener mPageStateListener;

    /**
     * webview初始化
     *
     * @param context
     */
    public static void initWebView(final Context context) {
        //Log.i("caicai", "WebAppliction initWebView");
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);

        final String vendors = FileUtil.getJsStr(context, "web-framework.js");
        final String jsStr = FileUtil.getJsStr(context, "dsbridge.js");
        mWebView = new AlitaNativeWebView(context);
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
                if (mPageStateListener != null){
                    mPageStateListener.onPageStarted(url);
                }
                mWebView.evaluateJavascript(vendors, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                       // Log.i("caicai", "vendors " + value);
                    }
                });
                mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //Log.i("caicai", "jsStr " + value);
                    }
                });
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogUtil.i("caicai", "onPageFinished " + url);
                if (mPageStateListener != null){
                    mPageStateListener.onPageFinished(url);
                }
            }

            @Override
            public void onPageCommitVisible(WebView webView, String s) {
                super.onPageCommitVisible(webView, s);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
               // Log.i("caicai", "onLoadResource");
                mWebView.evaluateJavascript(vendors, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //Log.i("caicai", "vendors " + value);
                    }
                });
                mWebView.evaluateJavascript(jsStr, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //Log.i("caicai", "jsStr " + value);
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
    }

    private static WebResourceResponse getWebResourceResponse(Context context, String url) {
        WebResourceResponse res = null;
        try {
            InputStream instream = context.getResources().getAssets().open(
                    url);
            res = new WebResourceResponse("text/javascript",
                    "UTF-8", instream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 获取webview
     *
     * @return
     */
    public static AlitaNativeWebView getWebView() {
        return mWebView;
    }

    public static void setNeedClearHistory(boolean needClearHistory) {
        AlitaOtherAgent.needClearHistory = needClearHistory;
    }

    /**
     * 释放webview的缓存数据
     */
    public static void clearWebApp() {
        if (mWebView == null){
            return;
        }
        mWebView.clearCache(true);
        mWebView.clearFormData();
        mWebView.clearMatches();
        mWebView.clearSslPreferences();
        mWebView.clearDisappearingChildren();
        mWebView.clearHistory();
        mWebView.clearAnimation();
        //mWebView.loadUrl("about:blank");
        mWebView.removeAllViews();
        mWebView.freeMemory();
        mWebView = null;
    }

    //TODO 可扩展的js方法
    @SuppressLint("JavascriptInterface")
    public static void setBiridge(Object o, String name) {
        mWebView.addJavascriptObject(o, name);
    }


    public static void setPageStateListener(PageStateListener pageStateListener) {
        mPageStateListener = pageStateListener;
    }

    public interface PageStateListener{

        void onPageStarted(String url);

        void onPageFinished(String url);
    }
}
