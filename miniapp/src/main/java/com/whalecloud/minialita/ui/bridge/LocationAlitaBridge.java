package com.whalecloud.minialita.ui.bridge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.whalecloud.minialita.net.RequestBusiness;
import com.whalecloud.minialita.net.interior.ProgressCallBack;
import com.whalecloud.minialita.net.protocol.RequestProtocol;
import com.whalecloud.minialita.ui.activity.BaseMiniActivity;
import com.whalecloud.minialita.ui.activity.WebviewActivity;
import com.whalecloud.minialita.ui.web.CompletionHandler;

import org.json.JSONObject;

import java.io.File;

public class LocationAlitaBridge {

    BaseMiniActivity mActivity;

    public <T extends BaseMiniActivity> LocationAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    /**
     * 获取定位信息
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void getLocation(Object params, final CompletionHandler handler) {
        mActivity.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION},
                new BaseMiniActivity.OnRequestPermissionListen() {
                    @Override
                    public void succeed() {
                        try {
                            //获取系统的LocationManager对象
                            LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            // 判断GPS是否正常启动
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                // 返回开启GPS导航设置界面
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mActivity.startActivityForResult(intent, 0);
                                return;
                            }

                            Criteria criteria = new Criteria();
                            // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            // 设置是否要求速度
                            criteria.setSpeedRequired(false);
                            // 设置是否允许运营商收费
                            criteria.setCostAllowed(false);
                            // 设置是否需要方位信息
                            criteria.setBearingRequired(false);
                            // 设置是否需要海拔信息
                            criteria.setAltitudeRequired(false);
                            // 设置对电源的需求
                            criteria.setPowerRequirement(Criteria.POWER_LOW);
                            // 为获取地理位置信息时设置查询条件
                            String bestProvider = locationManager.getBestProvider(criteria, true);
                            //从GPS获取最新的定位信息
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            JSONObject jsonObject = new JSONObject();
                            //获取经度、纬度、等属性值
                            jsonObject.put("longitude",location.getLongitude());
                            jsonObject.put("latitude",location.getLatitude());
                            handler.complete(location);
                        }catch (Exception e){
                            e.printStackTrace();
                            handler.complete("Error");
                        }
                    }

                    @Override
                    public void fail() {
                        handler.complete("Error");
                    }
                });

    }
}
