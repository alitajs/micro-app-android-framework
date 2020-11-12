package com.alitajs.micro.net.interior;

import android.content.Context;

import com.alitajs.micro.net.interior.data.ConstantValue;
import com.alitajs.micro.net.interior.utils.LogUtil;


public class AlitaConfigure {

    /**
     * 日志开关
     * @param enable
     */
    public static void setLogEnable(boolean enable) {
        LogUtil.setLogLevel(enable ? LogUtil.DEBUG : LogUtil.NO_LOG);
    }

    /**
     * 初始化
     * @param context
     * @param appKey
     */
    public static void init(final Context context, String appKey){
       AlitaAgent.initWebView(context);
        ConstantValue.APP_KEY = appKey;
       //TODO 微应用初始化

    }

}
