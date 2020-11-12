package com.whalecloud.minialita.net.interior;


import com.whalecloud.minialita.net.protocol.ProtocolWrapper;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 请求数据接口，用于隔断HTTP层
 */
public interface RequestClient {

    <T>void get(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber);

    <T>void post(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber);

    <T>void json(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber);

    <T>void upload(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber);

    Disposable download(final ProtocolWrapper protocol, final ProgressCallBack callBack);
}
