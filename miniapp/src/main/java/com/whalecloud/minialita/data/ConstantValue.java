package com.whalecloud.minialita.data;

import android.os.Environment;

public class ConstantValue {

    public static final String TEMP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Alita";

    public static final String PHOTO_TEMP_PATH = TEMP_PATH + "/temp/";

    public static final String BASE_URL = "http://47.92.108.46:8009/api/";


    /**
     * 多媒体相关常量
     **/
    //启动扫码页面
    public static final int OPEN_SCAN_REQ_CODE = 1004;
    //启动拍照
    public static final int OPEN_CAMER_REQ_CODE = 1005;
    //打开手机相册
    public static final int OPEN_ALBUM_REQ_CODE = 1006;


    /**
     * 基础功能体相关常量
     **/
    //设置导航栏
    public static final int MESSAGE_TYPE_NARBAR = 1001;
    //设置导航栏标题
    public static final int MESSAGE_TYPE_NARBAR_TITLE = 1002;
    //设置导航栏返回显示/隐藏
    public static final int MESSAGE_TYPE_NARBAR_BACK_VISIBLE = 1003;
    //设置导航栏返回点击事件监听
    public static final int MESSAGE_TYPE_NARBAR_BACK_EVENT = 1004;
    //返回事件处理
    public static final int MESSAGE_TYPE_NARBAR_BACK = 1005;
    //关闭事件处理
    public static final int MESSAGE_TYPE_NARBAR_CLOSE = 1006;

}
