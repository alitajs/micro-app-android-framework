package com.alitajs.micro;

import android.content.Context;

import com.alitajs.micro.data.ConstantValue;
import com.alitajs.micro.utils.LogUtil;


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
