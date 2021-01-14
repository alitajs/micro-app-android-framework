package com.alitajs.micro.ui.bridge;

import android.webkit.JavascriptInterface;

import com.alitajs.micro.bean.CompletionBean;
import com.alitajs.micro.event.NoticeEvent;
import com.alitajs.micro.ui.activity.BaseMiniActivity;
import com.alitajs.micro.ui.web.CompletionHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

public class NoticeAlitaBridge {

    BaseMiniActivity mActivity;

    public <T extends BaseMiniActivity> NoticeAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    /**
     * 获取定位信息
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void postMessage(Object params, final CompletionHandler handler) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(params.toString());
            String name = jsonObject.optString("name", "");
            String userInfo = jsonObject.optString("userInfo","");
            EventBus.getDefault().post(new NoticeEvent(name, userInfo));
            handler.complete(new CompletionBean(0, "发送通知成功", "").getResult());
        } catch (JSONException e) {
            e.printStackTrace();
            handler.complete(new CompletionBean(3, e.toString(), "").getResult());
        }
    }
}
