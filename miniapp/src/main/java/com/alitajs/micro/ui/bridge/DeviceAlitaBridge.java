package com.alitajs.micro.ui.bridge;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alitajs.micro.AlitaAgent;
import com.alitajs.micro.BuildConfig;
import com.alitajs.micro.AlitaManager;
import com.alitajs.micro.bean.CompletionBean;
import com.alitajs.micro.bean.MicorAppBean;
import com.alitajs.micro.data.ConstantValue;
import com.alitajs.micro.ui.activity.BaseMiniActivity;
import com.alitajs.micro.ui.activity.MicroAppActivity;
import com.alitajs.micro.ui.activity.ScanCodeActivity;
import com.alitajs.micro.ui.activity.WebviewActivity;
import com.alitajs.micro.ui.web.CompletionHandler;
import com.alitajs.micro.utils.ScreenUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class DeviceAlitaBridge {

    BaseMiniActivity mActivity;
    Handler mHandler;

    CompletionHandler mCompletionHandler;
    JSONObject userData;

    public <T extends BaseMiniActivity> DeviceAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    public void setUserData(JSONObject userData) {
        this.userData = userData;
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                //扫码返回
                if (ConstantValue.OPEN_SCAN_REQ_CODE == requestCode) {
                    String result = data.getStringExtra("codedContent");
                    //TODO 返回扫描结果
                    if (mCompletionHandler != null) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("result", result);
                            mCompletionHandler.complete(new CompletionBean(0, "扫码成功", jsonObject).getResult());
                            //mHandler.complete(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mCompletionHandler.complete(new CompletionBean(3, "扫码错误", jsonObject).getResult());
                        } finally {
                            return;
                        }
                    }
                }
            }
        }
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
    public void platform(Object params, final CompletionHandler handler) {
        try {
            PackageInfo packageinfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_INSTRUMENTATION);
            String ANDROID_ID = Settings.System.getString(mActivity.getContentResolver(), Settings.System.ANDROID_ID);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("platform", "android");
            jsonObject.put("version", packageinfo.versionName);//主app版本
            jsonObject.put("uuid", ANDROID_ID);//设备唯一标识
            jsonObject.put("stausBarHeight", ScreenUtil.dip2px(mActivity, 50));//导航栏高度
            jsonObject.put("SDKVersion", BuildConfig.VERSION_NAME);//sdk版本号

            handler.complete(new CompletionBean(0, "获取成功", jsonObject).getResult());
        } catch (Exception e) {
            handler.complete("Error");
            handler.complete(new CompletionBean(3, "获取失败", "").getResult());
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
            handler.complete(new CompletionBean(3, "Error", "").getResult());
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
        if (userData != null){
            handler.complete(new CompletionBean(0, "获取成功", userData).getResult());
        }else {
            handler.complete(new CompletionBean(3, "用户数据为空", "").getResult());
        }
    }

    /**
     * 扫码
     *
     * @param params onlyFromCamera	boolean	false	否	是否只能从相机扫码，不允许从相册选择图片
     *               scanType	Array<string>	['qrCode']	否	扫码类型，目前只支持二维码
     */
    @JavascriptInterface
    public void scanCode(final Object params, final CompletionHandler handler) {
        Log.i("caicai", "scanCode");
        //权限请求
        mActivity.requestPermission(new String[]{Manifest.permission.CAMERA}, new BaseMiniActivity.OnRequestPermissionListen() {
            @Override
            public void succeed() {
                //TODO 参数解析后具体处理
                try {
                    JSONObject jsonObject = new JSONObject(params.toString());
                    boolean onlyFromCamera = jsonObject.optBoolean("onlyFromCamera");
                    mCompletionHandler = handler;
                    Intent intent = new Intent(mActivity, ScanCodeActivity.class);
                    intent.putExtra("onlyFromCamera", onlyFromCamera);
                    mActivity.startActivityForResult(intent, ConstantValue.OPEN_SCAN_REQ_CODE);
                } catch (Exception e) {
                    handler.complete(new CompletionBean(3, e.toString(), "").getResult());
                }
            }

            @Override
            public void fail() {
                Toast.makeText(mActivity, "请同意拍照权限后操作", Toast.LENGTH_SHORT).show();
                handler.complete(new CompletionBean(3, "未开启拍照权限", "").getResult());
            }
        });
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
        AlitaManager.getInstance(mActivity).getMicorAppList(new AlitaManager.RequestCallback() {
            @Override
            public void onError(String errorCode, String errorMessage) {
                handler.complete("Error");
            }

            @Override
            public void onSuccess(ArrayList<MicorAppBean.MicorAppData> records) {
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < records.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        MicorAppBean.MicorAppData appData = records.get(i);
                        jsonObject.put("appid", appData.appid);
                        jsonObject.put("appsecret", appData.appsecret);
                        jsonObject.put("appName", appData.appName);
                        jsonObject.put("appDesc", appData.appDesc);
                        jsonObject.put("appIconUrl", appData.appIconUrl);
                        jsonObject.put("versionId", appData.versionId);
                        jsonArray.put(jsonObject);
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("list", jsonArray);
                    handler.complete(new CompletionBean(0, "获取成功", jsonObject).getResult());
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.complete(new CompletionBean(3, e.toString(), "").getResult());
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
            userData = jsonObject.optJSONObject("userData");
            JSONObject app = jsonObject.optJSONObject("app");
            if (app != null){
                String versionId = app.optString("versionId", "");
                String appName = app.optString("appName", "");
                String appid = app.optString("appid", "");
                MicorAppBean.MicorAppData appData = new MicorAppBean.MicorAppData();
                appData.versionId = versionId;
                appData.appid = appid;
                appData.appName = appName;
                AlitaManager.getInstance(mActivity).startMicorApp(appData, userData.toString(), null);
                return;
            }
            String appURL = jsonObject.optString("appURL");
            if (!TextUtils.isEmpty(appURL)){
                AlitaAgent.getWebView().loadUrl(appURL);
                Intent intent = new Intent(mActivity, MicroAppActivity.class);
                intent.putExtra("userData", userData.toString());
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                mActivity.startActivity(intent);
                handler.complete(new CompletionBean(0, "启动成功", "").getResult());
            }else {
                handler.complete(new CompletionBean(3, "链接为空", "").getResult());
            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.complete(new CompletionBean(3, e.toString(), "").getResult());
        }
    }

}
