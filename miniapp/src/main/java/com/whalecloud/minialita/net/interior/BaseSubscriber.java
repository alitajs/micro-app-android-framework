package com.whalecloud.minialita.net.interior;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public abstract class BaseSubscriber<T> implements Observer<T> {

    private boolean isNeedCahe;

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        onError(ExceptionHandle.handleException(e));
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        // todo some common as show loadding  and check netWork is NetworkAvailable
//        if (!TelephoneUtil.isNetworkAvailable(BaseApplication.getContext())){
//            //TODO
//        }
    }

    @Override
    public void onComplete() {
        // todo some common as  dismiss loadding

    }

    public abstract void onError(ExceptionHandle.RespondThrowable e);

}
