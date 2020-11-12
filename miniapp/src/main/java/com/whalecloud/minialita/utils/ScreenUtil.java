package com.whalecloud.minialita.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class ScreenUtil {

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(DisplayMetrics metrics, float dipValue) {
        final float scale = metrics.density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(DisplayMetrics metrics, float pxValue) {
        final float scale = metrics.density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float getValue(Context c, int unit, float size) {
        Resources r;
        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }
        return TypedValue.applyDimension(
                unit, size, r.getDisplayMetrics());
    }

    /**
     * 判断是否横屏
     *
     * @param context
     * @return
     */
    public static boolean isOrientationLandscape(Context context) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        return false;
    }

    /**
     * 返回屏幕尺寸
     *
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * 返回屏幕尺寸
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return metrics.heightPixels;
    }

    /**
     * 转换密度值成服务端值 "0"表示密度为1 "1"表示密度为1.5
     *
     * @param density
     * @return
     */
    public static String convertDensitytoServerValue(float density) {
        String SERVER_MEDIUM_DENSITY = "0";
        String SERVER_HIGH_DENSITY = "1";
        String serverDensity = SERVER_MEDIUM_DENSITY;
        if (density == 1.0) {
            serverDensity = SERVER_MEDIUM_DENSITY;
        } else if (density == 1.5) {
            serverDensity = SERVER_HIGH_DENSITY;
        } else if (density == 2) {
            serverDensity = SERVER_HIGH_DENSITY;
        } else {
            serverDensity = SERVER_MEDIUM_DENSITY;
        }
        return serverDensity;
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

}

