package com.alitajs.micro.net.interior;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.alitajs.micro.net.interior.bean.WebAppBean;
import com.alitajs.micro.net.interior.data.ConstantValue;
import com.alitajs.micro.net.interior.net.RequestBusiness;
import com.alitajs.micro.net.interior.net.interior.BaseResponse;
import com.alitajs.micro.net.interior.net.interior.BaseSubscriber;
import com.alitajs.micro.net.interior.net.interior.ExceptionHandle;
import com.alitajs.micro.net.interior.net.interior.ProgressCallBack;
import com.alitajs.micro.net.interior.net.protocol.RequestProtocol;
import com.alitajs.micro.net.interior.ui.activity.MicroAppActivity;
import com.alitajs.micro.net.interior.utils.FileUtil;
import com.alitajs.micro.net.interior.utils.LogUtil;
import com.alitajs.micro.net.interior.utils.ZipUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlitaManager {

    Activity mActivity;
    String mUserData;

    String dir;
    String appPath;
    String zipPath;
    String htmlPath;
    String fileName = "miniApp";

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
        dir = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/" + activity.getPackageName() + "/WebApp/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 启动微应用
     *
     * @param versionId
     * @param appName
     * @param miniAppId
     * @param version
     */
    public void startWebApp(String versionId, String appName, String miniAppId, String version, String userData) {
        this.mUserData = userData;
        if (TextUtils.isEmpty(versionId)){
            Toast.makeText(mActivity,"暂无上线版本", Toast.LENGTH_SHORT).show();
            return;
        }
        //权限请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
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
        appPath = dir + miniAppId + "/" + version;
        zipPath = appPath + "/" + fileName + ".zip";
        File file = new File(zipPath);
        if (!file.exists()){
            download(ConstantValue.BASE_URL + "version/download/?versionId=" + versionId,miniAppId, version);
        }else {
            startWebActivity();
        }
    }


    /**
     * 获取微应用列表
     *
     * @return
     */
    public void getWebAppList(final RequestCallback callback) {
        if (TextUtils.isEmpty(ConstantValue.APP_KEY)){
            Toast.makeText(mActivity, "请初始化SDK后操作",Toast.LENGTH_SHORT).show();
            return;
        }
        RequestProtocol protocol = new RequestProtocol("/api/microApp/queryListByPage");
        protocol.put("belongToApp", ConstantValue.APP_KEY);
        RequestBusiness business = new RequestBusiness();
        business.json(protocol, new BaseSubscriber<BaseResponse<WebAppBean>>() {
            @Override
            public void onError(ExceptionHandle.RespondThrowable e) {
                if (callback != null)
                    callback.onError(e.code, e.message);
            }

            @Override
            public void onNext(BaseResponse<WebAppBean> response) {
                WebAppBean webAppBean = response.getData(WebAppBean.class);
                if (callback != null)
                    callback.onSuccess(webAppBean.records);
            }
        });
    }

    /**
     * 下载微应用
     */
    private void download(String dwonloadUrl, final String miniAppId, final String version) {
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
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "下载失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCompleted(File file, long fileSize) {
                LogUtil.i("caicai", "downloaw end");
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "下载成功", Toast.LENGTH_SHORT).show();
                    }
                });
                startWebActivity();
                //TODO 删除旧版包
                deleteWebApp(dir + miniAppId, version);
            }

            @Override
            public void onProgress(long total, long current) {
                super.onProgress(total, current);
                //TODO 是否需要进度条
            }
        });
    }

    /**
     * 删除除最新版之外的文件夹
     * @param path
     * @param version
     */
    private void deleteWebApp(String path, String version) {
        File root = new File(path);
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (!f.getAbsolutePath().contains(version)){
                    FileUtil.delele(f.getPath());
                }
            }
    }

    /**
     * 启动页面
     */
    private void startWebActivity() {
        htmlPath = "file:///" + appPath + "/" + fileName;
        //已下载已解压
        File htmlFile = new File(appPath  + "/" + fileName + "/" );
        if (htmlFile.exists()) {
            String url = htmlPath;
            if (htmlPath.startsWith("file:///") && !htmlPath.contains("js-call-native")){
                url += "/dist/index.html";
            }
            //MiniAppAgent.getWebView().clearHistory();
            AlitaAgent.getWebView().loadUrl(url);
            Intent intent = new Intent(mActivity, MicroAppActivity.class);
            intent.putExtra("htmlPath", htmlPath);
            intent.putExtra("userData",mUserData);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            mActivity.startActivity(intent);
        }else {
            //存在未解压的情况
            File zipFile = new File(zipPath);
            if (zipFile.exists()) {
                try {
                    //解压
                    ZipUtils.UnZipFolder(zipPath, appPath  + "/" + fileName + "/" );
                    startWebActivity();
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO 解压失败，删除文件重新下载
                }
            }
        }
    }

    public interface RequestCallback{
        //微应用列表回调
        void onError(String errorCode, String errorMessage);

        void onSuccess(ArrayList<WebAppBean.WebAppData> records);
    }
}