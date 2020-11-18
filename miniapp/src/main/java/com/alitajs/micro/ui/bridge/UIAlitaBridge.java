package com.alitajs.micro.ui.bridge;

import android.os.Handler;
import android.os.Message;
import android.webkit.JavascriptInterface;

import com.alitajs.micro.data.ConstantValue;
import com.alitajs.micro.ui.activity.BaseMiniActivity;
import com.alitajs.micro.ui.web.CompletionHandler;


public class UIAlitaBridge {

    BaseMiniActivity mActivity;
    Handler mHandler;

    CompletionHandler mCompletionHandler;

    public <T extends BaseMiniActivity> UIAlitaBridge(T activity) {
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
     * 设置webview背景色
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void setBackgroundColor(Object params, CompletionHandler handler) {
        sendMessage(ConstantValue.MESSAGE_TYPE_NARBAR, params);
    }

}
