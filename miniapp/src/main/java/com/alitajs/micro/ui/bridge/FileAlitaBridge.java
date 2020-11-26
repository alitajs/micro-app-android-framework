package com.alitajs.micro.ui.bridge;

import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alitajs.micro.bean.CompletionBean;
import com.alitajs.micro.net.RequestBusiness;
import com.alitajs.micro.net.interior.ProgressCallBack;
import com.alitajs.micro.net.protocol.RequestProtocol;
import com.alitajs.micro.ui.activity.BaseMiniActivity;
import com.alitajs.micro.ui.activity.WebviewActivity;
import com.alitajs.micro.ui.dialog.LoadingDialog;
import com.alitajs.micro.ui.web.CompletionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;

public class FileAlitaBridge {

    BaseMiniActivity mActivity;
    LoadingDialog mLoadingDialog;

    public <T extends BaseMiniActivity> FileAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    /**
     * 在线预览office文件
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void openDocument(final Object params, final CompletionHandler handler){
        //TODO https://juejin.im/post/6844903575059955726
        // https://my.oschina.net/u/4312036/blog/4318368
        // https://www.cnblogs.com/wangfeng520/p/7814974.html
        try {
            String url = "";
            try {
                JSONObject jsonObject = new JSONObject(params.toString());
                url = jsonObject.optString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(url)){
                Toast.makeText(mActivity,"文件路径无效", Toast.LENGTH_SHORT).show();
                return;
            }
            String docUrl = "https://view.officeapps.live.com/op/view.aspx?src=" + url;
            Intent intent = new Intent(mActivity, WebviewActivity.class);
            intent.putExtra("htmlPath", docUrl);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            mActivity.startActivity(intent);
            handler.complete(new CompletionBean(0, "启动成功", "").getResult());
        } catch (Exception e) {
            handler.complete(new CompletionBean(3, e.getMessage(), "").getResult());
        }
    }

    /**
     * 保存文件
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void saveFile(final Object params, final CompletionHandler handler){
        String url = "";
        try {
            JSONObject jsonObject = new JSONObject(params.toString());
            url = jsonObject.optString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(url)){
            Toast.makeText(mActivity,"文件下载路径无效", Toast.LENGTH_SHORT).show();
            handler.complete(new CompletionBean(3, "文件下载路径无效", "").getResult());
            return;
        }
        //TODO
        showLoadingDialog();
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/" + mActivity.getPackageName() + "/Download/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String[] files = params.toString().split("/");
        final String path = dir + files[files.length - 1];
        RequestBusiness business = new RequestBusiness();
        RequestProtocol protocol = new RequestProtocol(url);
        protocol.putSavePath(path);
        protocol.build();
        business.download(protocol, new ProgressCallBack() {
            @Override
            public void onError(final Throwable e) {
                // TODO 主线程
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "下载失败，请重试" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        handler.complete(new CompletionBean(3, "下载失败", "").getResult());
                        dismissLoadingDialog();
                    }
                });
            }

            @Override
            public void onCompleted(File file, long fileSize) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "文件保存在" + path, Toast.LENGTH_SHORT).show();
                        handler.complete(new CompletionBean(0, "下载成功", "").getResult());
                        dismissLoadingDialog();
                    }
                });
            }

            @Override
            public void onProgress(long total, long current) {
                super.onProgress(total, current);
                //TODO 是否需要进度条
            }
        });
    }

    private void showLoadingDialog() {
        if (mLoadingDialog != null) {
          mLoadingDialog = new LoadingDialog(mActivity);
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }
}
