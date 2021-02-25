package com.alitajs.micro;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.alitajs.micro.bean.MicorAppBean;
import com.alitajs.micro.bean.ThemeBean;
import com.alitajs.micro.data.ConstantValue;
import com.alitajs.micro.net.RequestBusiness;
import com.alitajs.micro.net.interior.BaseResponse;
import com.alitajs.micro.net.interior.BaseSubscriber;
import com.alitajs.micro.net.interior.ExceptionHandle;
import com.alitajs.micro.net.interior.ProgressCallBack;
import com.alitajs.micro.net.protocol.RequestProtocol;
import com.alitajs.micro.ui.activity.MicroAppActivity;
import com.alitajs.micro.ui.dialog.LoadingDialog;
import com.alitajs.micro.utils.FileUtil;
import com.alitajs.micro.utils.LogUtil;
import com.alitajs.micro.utils.ZipUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlitaManager {

    Activity mActivity;
    String mUserData;
    ThemeBean mThemeBean;

    String dir;
    String appPath;
    String appVersionPath;
    String zipPath;
    String htmlPath;
    String fileName = "miniApp";

    int mLoadingColor;

    LoadingDialog mLoadingDialog;

    static volatile AlitaManager instance;

    public static AlitaManager getInstance(Activity activity) {
        if (instance == null) {
            synchronized (AlitaManager.class) {
                if (instance == null) {
                    instance = new AlitaManager(activity);
                }
            }
        }
        return instance;
    }

    AlitaManager(Activity activity) {
        this.mActivity = activity;
        dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + activity.getPackageName() + "/WebApp/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void initLoadingDialog(Activity activity, int color) {
        mLoadingDialog = new LoadingDialog(activity);
        mLoadingColor = color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLoadingDialog.create();
        }
        mLoadingDialog.initColorRes(color);
    }

    public void initLoadingDialog(Activity activity) {
        mLoadingDialog = new LoadingDialog(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLoadingDialog.create();
        }
        mLoadingDialog.initColorRes(mLoadingColor == 0 ? R.color.bg_black : mLoadingColor);
    }

    private void showLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    public void startWebView(String url, String userData) {
        AlitaAgent.getWebView().loadUrl(url);
        Intent intent = new Intent(mActivity, MicroAppActivity.class);
        intent.putExtra("theme", mThemeBean);
        intent.putExtra("userData", userData);
        intent.putExtra("needTopbar", false);
        mActivity.startActivity(intent);
    }

    /**
     * 启动微应用
     *
     * @param appData
     */
    public void startMicorApp(MicorAppBean.MicorAppData appData, String userData) {
        startMicorApp(appData, userData, null);
    }

    /**
     * 启动微应用
     *
     * @param appData
     * @param downloadCallback
     */
    public void startMicorApp(final MicorAppBean.MicorAppData appData, final String userData, final DownloadCallback downloadCallback) {
        this.mUserData = userData;
        if (downloadCallback == null)
            showLoadingDialog();
        //查询一个微应用
        getMicorAppData(appData.appid, new BaseSubscriber<BaseResponse<MicorAppBean.MicorAppData>>() {
            @Override
            public void onError(ExceptionHandle.RespondThrowable e) {
                startMicorAppTask(appData, userData, downloadCallback);
            }

            @Override
            public void onNext(BaseResponse<MicorAppBean.MicorAppData> response) {
                MicorAppBean.MicorAppData data = response.getData(MicorAppBean.MicorAppData.class);
                startMicorAppTask(data, userData, downloadCallback);
            }
        });

    }


    private void startMicorAppTask(MicorAppBean.MicorAppData appData, String userData, DownloadCallback downloadCallback) {
        //AlitaAgent.getWebView().loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        // AlitaAgent.getWebView().clearHistory();
        if (TextUtils.isEmpty(appData.versionId)) {
            Toast.makeText(mActivity, "暂无上线版本", Toast.LENGTH_SHORT).show();
            return;
        }
        //权限请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> lackedPermission = new ArrayList<String>();
            if (!(mActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (lackedPermission.size() != 0) {
                String[] requestPermissions = new String[lackedPermission.size()];
                lackedPermission.toArray(requestPermissions);
                mActivity.requestPermissions(requestPermissions, 1024);
                return;
            }
        }
        //TODO 判断文件是否存在 版本对比 下载，解压，启动
        appPath = dir + appData.appid;
        appVersionPath = appPath + "/" + appData.versionId;
        zipPath = appVersionPath + "/" + fileName + ".zip";
        File file = new File(zipPath);
        if (!file.exists()) {
            download(ConstantValue.BASE_URL + "version/download/?versionId=" + appData.versionId, appData.appid, appData.versionId, downloadCallback);
        } else {
            startWebActivity();
        }
    }

    /**
     * 获取微应用列表
     *
     * @return
     */
    public void getMicorAppList(final RequestCallback callback) {
        if (TextUtils.isEmpty(ConstantValue.APP_KEY)) {
            Toast.makeText(mActivity, "请初始化SDK后操作", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestProtocol protocol = new RequestProtocol("/api/app/microApp/queryListByPage");
        protocol.put("belongToApp", ConstantValue.APP_KEY);
        RequestBusiness business = new RequestBusiness();
        business.json(protocol, new BaseSubscriber<BaseResponse<MicorAppBean>>() {
            @Override
            public void onError(ExceptionHandle.RespondThrowable e) {
                if (callback != null)
                    callback.onError(e.code, e.message);
            }

            @Override
            public void onNext(BaseResponse<MicorAppBean> response) {
                MicorAppBean micorAppBean = response.getData(MicorAppBean.class);
                if (callback != null)
                    callback.onSuccess(micorAppBean.records);
            }
        });
    }

    /**
     * 查询一个微应用
     *
     * @param appid
     * @param subscriber
     */
    private void getMicorAppData(String appid, BaseSubscriber<BaseResponse<MicorAppBean.MicorAppData>> subscriber) {
        RequestProtocol protocol = new RequestProtocol("/api/app/microApp/queryOne");
        protocol.put("appid", appid);
        RequestBusiness business = new RequestBusiness();
        business.json(protocol, subscriber);
    }

    /**
     * 下载微应用
     */
    private void download(String dwonloadUrl, final String miniAppId, final String version, final DownloadCallback downloadCallback) {
        LogUtil.i("caicai", "download start");
        File file = new File(dir + miniAppId + "/" + version);
        if (!file.exists()) {
            file.mkdirs();
        }
        RequestBusiness business = new RequestBusiness();
        RequestProtocol protocol = new RequestProtocol(dwonloadUrl);
        protocol.putSavePath(zipPath);
        protocol.build();
        business.download(protocol, new ProgressCallBack() {
            @Override
            public void onError(final Throwable e) {
                // TODO 主线程
                if (downloadCallback != null) {
                    downloadCallback.onError(e.getMessage());
                } else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            Toast.makeText(mActivity, "下载失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCompleted(File file, long fileSize) {
                LogUtil.i("caicai", "downloaw end");
                if (downloadCallback != null) {
                    downloadCallback.onSuccess();
                } else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            Toast.makeText(mActivity, "下载成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                startWebActivity();
                //删除旧版包
                deleteWebApp(dir + miniAppId, version);
            }

            @Override
            public void onProgress(long total, long current) {
                super.onProgress(total, current);
                //进度条回调
                if (downloadCallback != null) {
                    downloadCallback.onProgress(total, current);
                }
            }
        });
    }

    /**
     * 删除除最新版之外的文件夹
     *
     * @param path
     * @param version
     */
    private void deleteWebApp(String path, String version) {
        File root = new File(path);
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (!f.getAbsolutePath().contains(version)) {
                    FileUtil.delele(f.getPath());
                }
            }
    }

    /**
     * 启动页面
     */
    private void startWebActivity() {
        dismissLoadingDialog();
        htmlPath = "file:///" + appVersionPath + "/" + fileName;
        //已下载已解压
        File htmlFile = new File(appVersionPath + "/" + fileName + "/");
        if (htmlFile.exists()) {
            String url = htmlPath;
            if (htmlPath.startsWith("file:///") && !htmlPath.contains("js-call-native")) {
                url += "/dist/index.html";
            }
            AlitaAgent.getWebView().loadUrl(url);
            Intent intent = new Intent(mActivity, MicroAppActivity.class);
            intent.putExtra("htmlPath", htmlPath);
            intent.putExtra("userData", mUserData);
            intent.putExtra("theme", mThemeBean);
            intent.putExtra("url", url);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            mActivity.startActivity(intent);
        } else {
            //存在未解压的情况
            File zipFile = new File(zipPath);
            if (zipFile.exists()) {
                try {
                    String unZipPath = appVersionPath + "/" + fileName + "/";
                    //解压
                    ZipUtils.UnZipFolder(zipPath, unZipPath);
                    //TODO 判断解压文件是否完整
                    boolean isUnZipFilesExists = true;
                    File icon = new File(unZipPath + "icon.png");
                    if (!icon.exists()){
                        isUnZipFilesExists = false;
                    }
                    File json = new File(unZipPath + "asset-manifest.json");
                    if (!json.exists()){
                        isUnZipFilesExists = false;
                    }
                    File dist = new File(unZipPath + "dist");
                    if (!dist.exists()){
                        isUnZipFilesExists = false;
                    }

                    if (isUnZipFilesExists){
                        startWebActivity();
                    }else {
                        deleteAppFile();
                        Toast.makeText(mActivity, "下载失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO 解压失败，删除文件 提示重新下载
                    deleteAppFile();
                    Toast.makeText(mActivity, "解压失败，请重新下载", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void deleteAppFile(){
        File root = new File(appPath);
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                FileUtil.delele(f.getPath());
            }
    }

    public void setThemeBean(ThemeBean themeBean) {
        this.mThemeBean = themeBean;
    }

    //微应用列表回调
    public interface RequestCallback {

        void onError(String errorCode, String errorMessage);

        void onSuccess(ArrayList<MicorAppBean.MicorAppData> records);
    }

    //下载回调
    public interface DownloadCallback {

        void onError(String errorMessage);

        void onSuccess();

        void onProgress(long total, long current);
    }
}
