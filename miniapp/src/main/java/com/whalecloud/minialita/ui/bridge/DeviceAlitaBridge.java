package com.whalecloud.minialita.ui.bridge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.whalecloud.minialita.BuildConfig;
import com.whalecloud.minialita.MiniAppManager;
import com.whalecloud.minialita.bean.WebAppBean;
import com.whalecloud.minialita.data.ConstantValue;
import com.whalecloud.minialita.ui.activity.BaseMiniActivity;
import com.whalecloud.minialita.ui.activity.WebviewActivity;
import com.whalecloud.minialita.ui.web.CompletionHandler;
import com.whalecloud.minialita.utils.ScreenUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class DeviceAlitaBridge {

    BaseMiniActivity mActivity;
    Handler mHandler;

    public <T extends BaseMiniActivity> DeviceAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    private void sendMessage(int what, Object params) {
        Message message = new Message();
        message.what = what;
        message.obj = params;
        mHandler.sendMessage(message);
    }

    /**
     * 设置导航栏
     *
     * @param params backgroundColor: '#FFF', // 背景颜色
     *               color:'#000', // 标题字体颜色
     *               fontSize:'24', // 标题字号
     */
    @JavascriptInterface
    public void setNavBar(Object params, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR, params);
    }

    /**
     * 设置导航栏标题
     *
     * @param title
     */
    @JavascriptInterface
    public void setNavTitle(Object title, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR_TITLE, title);
    }

    //TODO 返回事件监听，返回，关闭，清除历史，返回键显示隐藏，
    //TODO 启动拨打电话，启动第三方app，

    /**
     * 返回事件监听
     */
    @JavascriptInterface
    public void onBackEvent(Object args, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR_BACK_EVENT, args);
    }

    /**
     * 返回
     */
    @JavascriptInterface
    public void goBack(Object args, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR_BACK, args);
    }

    /**
     * 关闭
     */
    @JavascriptInterface
    public void closeActivity(Object args, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR_CLOSE, args);
    }

    /**
     * 显示/隐藏 返回按钮
     */
    @JavascriptInterface
    public void hideBackView(Object args, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR_BACK_VISIBLE, args);
    }


    /**
     * 获取系统信息
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void systemInfo(Object params, final CompletionHandler handler) {
        try {
            PackageInfo packageinfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_INSTRUMENTATION);
            String ANDROID_ID = Settings.System.getString(mActivity.getContentResolver(), Settings.System.ANDROID_ID);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("platform", "android");
            jsonObject.put("version", packageinfo.versionName);//主app版本
            jsonObject.put("uuid", ANDROID_ID);//设备唯一标识
            jsonObject.put("stausBarHeight", ScreenUtil.dip2px(mActivity, 50));//导航栏高度
            jsonObject.put("SDKVersion", BuildConfig.VERSION_NAME);//sdk版本号
            handler.complete(jsonObject);
        } catch (Exception e) {
            handler.complete("Error");
        }
    }

    /**
     * 打开web页面
     *
     * @param url
     * @param handler
     */
    @JavascriptInterface
    public void openWeb(Object url, final CompletionHandler handler) {
        try {
            Intent intent = new Intent(mActivity, WebviewActivity.class);
            intent.putExtra("htmlPath", String.valueOf(url));
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            mActivity.startActivity(intent);
            handler.complete("Success");
        } catch (Exception e) {
            handler.complete("Error");
        }
    }

    /**
     * 获取原生参数
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void getUserData(Object params, final CompletionHandler handler) {

    }

    /**
     * 获取微应用列表
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void fetchMicroAppList(Object params, final CompletionHandler handler) {
        //TODO 判断appley是否为空
        MiniAppManager.getInstance(mActivity).getWebAppList("cba9dd7a896748fd99c4b6b4dc35b80e", new MiniAppManager.RequestCallback() {
            @Override
            public void onError(String errorCode, String errorMessage) {
                handler.complete("Error");
            }

            @Override
            public void onSuccess(ArrayList<WebAppBean.WebAppData> records) {
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < records.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        WebAppBean.WebAppData appData = records.get(i);
                        jsonObject.put("appid", appData.appid);
                        jsonObject.put("appsecret", appData.appsecret);
                        jsonObject.put("appName", appData.appName);
                        jsonObject.put("appDesc", appData.appDesc);
                        jsonObject.put("appIconUrl", appData.appIconUrl);
                        jsonObject.put("versionId", appData.versionId);
                        jsonArray.put(jsonObject);
                    }
                    handler.complete(jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.complete("Error");
                }
            }
        });
    }

    /**
     * 打开微应用
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void openMicroApp(Object params, final CompletionHandler handler) {
        try {

            JSONObject jsonObject = new JSONObject(params.toString());
            JSONObject userData = jsonObject.optJSONObject("userData");
            JSONObject app = jsonObject.optJSONObject("app");
            String versionId = app.optString("versionId", "");
            String appName = app.optString("appName", "");
            String appid = app.optString("appid", "");
            MiniAppManager.getInstance(mActivity).startWebApp(versionId, appName, appid, versionId);
        } catch (Exception e) {
            e.printStackTrace();
            handler.complete("Error");
        }
    }

}
