package com.whalecloud.minialita.net.interior;

import java.io.File;

public abstract class ProgressCallBack {
    public void onStart(){}

    abstract public void onError(Throwable e);

    public void onProgress(long total,long current){}

    abstract public void onCompleted(File file, long fileSize);
}
