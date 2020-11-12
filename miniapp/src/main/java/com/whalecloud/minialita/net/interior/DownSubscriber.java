package com.whalecloud.minialita.net.interior;




import com.whalecloud.minialita.utils.FileUtil;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 下载任务的观察者类
 * Created by TQ on 2018/5/8.
 */

public class DownSubscriber implements Observer<Integer> {
    private String mSavePath;
    private ProgressCallBack mCallBack;
    //    总长度
    private long mLength;
    //    已下载长度
    private long mCurrent;
    //    是否已经显示过错误信息
    private boolean isShowError;

    public DownSubscriber(String savePath, long length, ProgressCallBack callBack) {
        mSavePath = savePath;
        mCallBack = callBack;
        mLength = length;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (mCallBack != null) {
            mCallBack.onStart();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (mCallBack != null && !isShowError) {
            isShowError = true;
            mCallBack.onError(e);
        }
    }

    @Override
    public void onComplete() {
        File file = FileUtil.getFile(mSavePath);
        if (mCallBack != null) {
            mCallBack.onCompleted(file, mLength);
        }
    }

    @Override
    public void onNext(final Integer read) {
        mCurrent += read;
        if (mCallBack != null) {
            mCallBack.onProgress(mLength, mCurrent);
        }
    }
}
