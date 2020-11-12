package com.whalecloud.minialita.ui.bridge;

import android.content.Intent;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.whalecloud.minialita.MiniAppAgent;
import com.whalecloud.minialita.net.RequestBusiness;
import com.whalecloud.minialita.net.interior.ProgressCallBack;
import com.whalecloud.minialita.net.protocol.RequestProtocol;
import com.whalecloud.minialita.ui.activity.BaseMiniActivity;
import com.whalecloud.minialita.ui.activity.WebviewActivity;
import com.whalecloud.minialita.ui.web.CompletionHandler;

import java.io.File;

public class FileAlitaBridge {

    BaseMiniActivity mActivity;

    public <T extends BaseMiniActivity> FileAlitaBridge(T activity) {
        this.mActivity = activity;
    }

    /**
     * 在线预览office文件
     * @param url
     * @param handler
     */
    @JavascriptInterface
    public void openDocument(final Object url, final CompletionHandler handler){
        //TODO https://juejin.im/post/6844903575059955726
        // https://my.oschina.net/u/4312036/blog/4318368
        // https://www.cnblogs.com/wangfeng520/p/7814974.html
        try {
            String docUrl = "https://view.officeapps.live.com/op/view.aspx?src=" + String.valueOf(url);
            Intent intent = new Intent(mActivity, WebviewActivity.class);
            intent.putExtra("htmlPath", docUrl);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            mActivity.startActivity(intent);
            handler.complete("Success");
        } catch (Exception e) {
            handler.complete("Error");
        }
    }

    /**
     * 保存文件
     * @param params
     * @param handler
     */
    @JavascriptInterface
    public void saveFile(final Object params, final CompletionHandler handler){
        //TODO
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/" + mActivity.getPackageName() + "/Download/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String[] files = params.toString().split("/");
        final String path = dir + files[files.length - 1];
        RequestBusiness business = new RequestBusiness();
        RequestProtocol protocol = new RequestProtocol(params.toString());
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

                    }
                });
                handler.complete("Error");
            }

            @Override
            public void onCompleted(File file, long fileSize) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "文件保存在" + path, Toast.LENGTH_SHORT).show();
                    }
                });
                handler.complete("Success");
            }

            @Override
            public void onProgress(long total, long current) {
                super.onProgress(total, current);
                //TODO 是否需要进度条
            }
        });
    }
}
