package com.whalecloud.minialita;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;

import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.whalecloud.minialita.ui.web.AlitaNativeWebView;
import com.whalecloud.minialita.utils.FileUtil;
import com.whalecloud.minialita.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MiniAppAgent {

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

        final String vendors = FileUtil.getJsStr(context, "vendors.js");
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
                LogUtil.i("caicai", "onPageFinished");
                if (mPageStateListener != null){
                    mPageStateListener.onPageFinished(url);
                }
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
                if (needClearHistory) {
                    mWebView.clearHistory();
                    needClearHistory = false;
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {
               /* if (url.indexOf("vendors.js") > -1) {
                    return getWebResourceResponse(context,"vendors.js");
                }*/
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
        MiniAppAgent.needClearHistory = needClearHistory;
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
        mWebView.loadUrl("about:blank");
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
