package com.whalecloud.minialita.utils;

import android.content.Context;
import android.util.Log;



/**
 * Created by zhangzhiqiang_dian91 on 2015/11/25.
 */
public class LogUtil {
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;
    public static final int NO_LOG = 5;

    public static int LOG_LEVEL = NO_LOG;
    public static boolean logFileEnable = false;

    private static LogUtil instance = null;

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    /**
     * 追加文件：使用FileWriter
     *
     * @param level
     * @param content
     */
    public static void write(String level, String content) {
        if (!logFileEnable)
            return;
    }

    private static LogUtil getInstance() {
        if (instance == null) {
            instance = new LogUtil();
        }
        return instance;
    }

    // verbose
    public static void v(String tag, String msg) {
        if (VERBOSE < LOG_LEVEL)
            return;
        Log.v(tag, msg);
        write("VERBOSE", msg);
    }

    public static void v(String msg) {
        v(getInstance().getFunctionName(), msg);
    }

    // debug
    public static void d(String tag, String msg) {
        if (DEBUG < LOG_LEVEL)
            return;
        Log.d(tag, msg);
        write("DEBUG", msg);
    }

    public static void d(String msg) {
        d(getInstance().getFunctionName(), msg);
    }

    // info
    public static void i(String tag, String msg) {
        if (INFO < LOG_LEVEL)
            return;
        Log.i(tag, msg);
        write("INFO", msg);
    }

    public static void i(String msg) {
        i(getInstance().getFunctionName(), msg);
    }

    // warning
    public static void w(String tag, String msg) {
        if (WARNING < LOG_LEVEL)
            return;
        Log.w(tag, msg);
        write("WARNING", msg);
    }

    public static void w(String msg) {
        w(getInstance().getFunctionName(), msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (WARNING < LOG_LEVEL)
            return;
        Log.w(tag, msg, tr);
        write("WARNING", msg);
    }

    // error
    public static void e(String tag, String msg) {
        if (ERROR < LOG_LEVEL)
            return;
        Log.e(tag, msg);
        write("ERROR", msg);
    }

    public static void e(String msg) {
        e(getInstance().getFunctionName(), msg);
    }

    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return "[ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName()
                    + " ]";
        }
        return null;
    }
}
