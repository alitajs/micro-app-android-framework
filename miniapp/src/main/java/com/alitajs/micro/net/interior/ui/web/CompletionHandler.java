package com.alitajs.micro.net.interior.ui.web;

/**
 * Created by du on 16/12/31.
 */

public interface  CompletionHandler<T> {
    void complete(T retValue);
    void complete();
    void setProgressData(T value);
}