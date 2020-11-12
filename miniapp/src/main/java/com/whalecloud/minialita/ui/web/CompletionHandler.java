package com.whalecloud.minialita.ui.web;

/**
 * Created by du on 16/12/31.
 */

public interface  CompletionHandler<T> {
    void complete(T retValue);
    void complete();
    void setProgressData(T value);
}
