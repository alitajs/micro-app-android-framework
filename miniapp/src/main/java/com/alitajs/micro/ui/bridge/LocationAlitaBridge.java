package com.alitajs.micro.ui.bridge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.webkit.JavascriptInterface;

import com.alitajs.micro.bean.CompletionBean;
import com.alitajs.micro.ui.activity.BaseMiniActivity;
import com.alitajs.micro.ui.web.CompletionHandler;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.json.JSONObject;

public class LocationAlitaBridge implements AMapLocationListener {

    BaseMiniActivity mActivity;
    CompletionHandler mHandler;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;

    public <T extends BaseMiniActivity> LocationAlitaBridge(T activity) {
        this.mActivity = activity;
        //初始化定位
        mLocationClient = new AMapLocationClient(activity);
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        AMapLocationClientOption option = new AMapLocationClientOption();
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        option.setNeedAddress(true);
        mLocationClient.setLocationOption(option);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*if (resultCode == 0) {
            if (ConstantValue.OPEN_GPS_REQ_CODE == requestCode) {
                location();
            }
        }*/
    }

    /**
     * 获取定位信息
     *
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void getLocation(Object params, final CompletionHandler handler) {
        this.mHandler = handler;
        mActivity.requestPermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                new BaseMiniActivity.OnRequestPermissionListen() {
                    @Override
                    public void succeed() {
                       /* if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        //获取系统的LocationManager对象
                        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
                        // 判断GPS是否正常启动
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            // 返回开启GPS导航设置界面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mActivity.startActivityForResult(intent, ConstantValue.OPEN_GPS_REQ_CODE);
                            return;
                        }
                        location();*/
                        if(null != mLocationClient){
                            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
                            mLocationClient.stopLocation();
                            mLocationClient.startLocation();
                        }
                    }

                    @Override
                    public void fail() {
                        mHandler.complete(new CompletionBean(3, "定位权限未打开", "").getResult());
                    }
                });

    }

    private void location() {
        try {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mHandler.complete(new CompletionBean(3, "GPS权限未打开", "").getResult());
                return;
            }
            //获取系统的LocationManager对象
            LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            // 设置是否要求速度
            criteria.setSpeedRequired(true);
            // 设置是否允许运营商收费
            criteria.setCostAllowed(true);
            // 设置是否需要方位信息
            criteria.setBearingRequired(true);
            // 设置是否需要海拔信息
            criteria.setAltitudeRequired(true);
            // 设置对电源的需求
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            // 为获取地理位置信息时设置查询条件
            String bestProvider = locationManager.getBestProvider(criteria, true);
            //从GPS获取最新的定位信息
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                mHandler.complete(new CompletionBean(3, "定位失败", "").getResult());
                return;
            }
            JSONObject jsonObject = new JSONObject();
            //获取经度、纬度、等属性值
            jsonObject.put("longitude", location.getLongitude());
            jsonObject.put("latitude", location.getLatitude());
            mHandler.complete(new CompletionBean(0, "定位成功", jsonObject).getResult());
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.complete(new CompletionBean(3, e.toString(), "").getResult());
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        try {
            if (aMapLocation == null) {
                mHandler.complete(new CompletionBean(3, "定位失败", "").getResult());
                return;
            }
            JSONObject jsonObject = new JSONObject();
            //获取经度、纬度、地址
            jsonObject.put("longitude", aMapLocation.getLongitude());
            jsonObject.put("latitude", aMapLocation.getLatitude());
            jsonObject.put("address", aMapLocation.getAddress());
            mHandler.complete(new CompletionBean(0, "定位成功", jsonObject).getResult());
        }catch (Exception e) {
            e.printStackTrace();
            mHandler.complete(new CompletionBean(3, e.toString(), "").getResult());
        }
    }
}
