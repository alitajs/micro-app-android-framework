package com.whalecloud.minialita.ui.bridge;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;


import com.lcw.library.imagepicker.ImagePicker;
import com.whalecloud.minialita.bean.AlbumParamBean;
import com.whalecloud.minialita.bean.CompletionBean;
import com.whalecloud.minialita.data.ConstantValue;
import com.whalecloud.minialita.net.RequestBusiness;
import com.whalecloud.minialita.net.interior.ProgressCallBack;
import com.whalecloud.minialita.net.protocol.RequestProtocol;
import com.whalecloud.minialita.ui.activity.BaseMiniActivity;
import com.whalecloud.minialita.ui.activity.ScanCodeActivity;
import com.whalecloud.minialita.ui.web.CompletionHandler;
import com.whalecloud.minialita.utils.BitmapUtil;
import com.whalecloud.minialita.utils.GlideLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class MediaAlitaBridge {

    BaseMiniActivity mActivity;

    CompletionHandler mHandler;
    AlbumParamBean mAlbumParamBean;

    public <T extends BaseMiniActivity> MediaAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                //扫码返回
                if (ConstantValue.OPEN_SCAN_REQ_CODE == requestCode) {
                    String result = data.getStringExtra("codedContent");
                    //TODO 返回扫描结果
                    if (mHandler != null) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("result", result);
                            mHandler.complete(new CompletionBean(0, "扫码成功", jsonObject).getResult());
                            //mHandler.complete(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mHandler.complete(new CompletionBean(3, "扫码错误", jsonObject).getResult());
                        } finally {
                            return;
                        }
                    }
                }

                //相册返回
                if (ConstantValue.OPEN_ALBUM_REQ_CODE == requestCode) {
                    List<String> imagePaths = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
                    //TODO 返回相册路径
                    if (mAlbumParamBean == null) {
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (int i = 0; i < imagePaths.size(); i++) {
                            String path = imagePaths.get(i);
                            JSONObject jsonObject = new JSONObject();

                            if (mAlbumParamBean.sizeType.contains("original"))//原图
                                jsonObject.put("path", path);

                            String compressedPath = null;
                            if (mAlbumParamBean.sizeType.contains("compressed")) {  //压缩图 分辨率宽度1080
                                compressedPath = BitmapUtil.compressImage(path, ConstantValue.PHOTO_TEMP_PATH + +System.currentTimeMillis() + i + ".png", 40);
                                jsonObject.put("thumbnail", compressedPath);
                            }

                            if (mAlbumParamBean.base64) {//base64
                                if (compressedPath == null)
                                    compressedPath = BitmapUtil.compressImage(path, ConstantValue.PHOTO_TEMP_PATH + +System.currentTimeMillis() + i + ".png", 40);
                                String base = BitmapUtil.imageToBase64(compressedPath);
                                jsonObject.put("base64", base);
                            }
                            jsonArray.put(jsonObject);

                        }
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("files", jsonArray);
                        mHandler.complete(new CompletionBean(0, "获取成功", jsonObject).getResult());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //拍照返回
            if (ConstantValue.OPEN_CAMER_REQ_CODE == requestCode) {
                //指定拍照临时文件路径
                //String photoPath = CommonConstant.CAMERA_TEMP_PATH;
                //TODO 返回拍照路径
            }
        }
    }

    /**
     * 扫码
     *
     * @param params onlyFromCamera	boolean	false	否	是否只能从相机扫码，不允许从相册选择图片
     *               scanType	Array<string>	['qrCode']	否	扫码类型，目前只支持二维码
     */
    @JavascriptInterface
    public void scanCode(final Object params, final CompletionHandler handler) {
        Log.i("caicai", "scanCode");
        //权限请求
        mActivity.requestPermission(new String[]{Manifest.permission.CAMERA}, new BaseMiniActivity.OnRequestPermissionListen() {
            @Override
            public void succeed() {
                //TODO 参数解析后具体处理
                try {
                    JSONObject jsonObject = new JSONObject(params.toString());
                    boolean onlyFromCamera = jsonObject.optBoolean("onlyFromCamera");
                    mHandler = handler;
                    Intent intent = new Intent(mActivity, ScanCodeActivity.class);
                    intent.putExtra("onlyFromCamera", onlyFromCamera);
                    mActivity.startActivityForResult(intent, ConstantValue.OPEN_SCAN_REQ_CODE);
                } catch (Exception e) {
                    handler.complete("Error");
                }
            }

            @Override
            public void fail() {
                Toast.makeText(mActivity, "请同意拍照权限后操作", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 打开手机相册
     *
     * @param params count	number	9	否	最多可以选择的图片张数
     *               sizeType	Array<string>	['original', 'compressed']	否	所选的图片的尺寸
     *               sourceType	Array<string>	['album', 'camera']	否	选择图片的来源
     *               base64	bool	true	否	是否需要 base64 数据
     */
    @JavascriptInterface
    public void chooseImage(final Object params, final CompletionHandler handler) {
        mActivity.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, new BaseMiniActivity.OnRequestPermissionListen() {
            @Override
            public void succeed() {
                mHandler = handler;
                mAlbumParamBean = new AlbumParamBean(params.toString());
                ImagePicker.getInstance()
                        .setTitle("选择图片")//设置标题
                        //.showCamera(true)//设置是否显示拍照按钮
                        .showImage(true)//设置是否展示图片
                        .showVideo(false)//设置是否展示视频
                        .setSingleType(true)//设置图片视频不能同时选择
                        .setMaxCount(mAlbumParamBean.count)//设置最大选择图片数目(默认为1，单选)
                        //.setImagePaths(mImageList)//保存上一次选择图片的状态，如果不需要可以忽略
                        .setImageLoader(new GlideLoader())//设置自定义图片加载器
                        .start(mActivity, ConstantValue.OPEN_ALBUM_REQ_CODE);//Intent调用的requestCode
            }

            @Override
            public void fail() {

            }
        });
    }

}
