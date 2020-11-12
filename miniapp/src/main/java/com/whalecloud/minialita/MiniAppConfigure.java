package com.whalecloud.minialita;

import android.content.Context;
import android.os.Environment;

import com.whalecloud.minialita.data.ConstantValue;
import com.whalecloud.minialita.utils.LogUtil;


public class MiniAppConfigure {

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
       MiniAppAgent.initWebView(context);
       //TODO 微应用初始化

    }

}
