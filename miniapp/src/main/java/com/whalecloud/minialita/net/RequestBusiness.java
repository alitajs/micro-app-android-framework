package com.whalecloud.minialita.net;

import com.whalecloud.minialita.net.interior.BaseApiService;
import com.whalecloud.minialita.net.interior.BaseInterceptor;
import com.whalecloud.minialita.net.interior.BaseResponse;
import com.whalecloud.minialita.net.interior.DownSubscriber;
import com.whalecloud.minialita.net.interior.ExceptionHandle;
import com.whalecloud.minialita.net.interior.ProgressCallBack;
import com.whalecloud.minialita.net.interior.RequestClient;
import com.whalecloud.minialita.net.interior.RequestHelper;
import com.whalecloud.minialita.net.interior.convert.CustomGsonConverterFactory;
import com.whalecloud.minialita.net.interior.cookie.CookieJarImpl;
import com.whalecloud.minialita.net.interior.cookie.store.PersistentCookieStore;
import com.whalecloud.minialita.net.protocol.ProtocolWrapper;
import com.whalecloud.minialita.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * 向服务端请求的封装，为请求逻辑构建一个底层
 */
public class RequestBusiness implements RequestClient {

    private static final int DEFAULT_TIMEOUT = 30;

    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    //.addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(CustomGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    private static OkHttpClient.Builder httpBuilder =
            new OkHttpClient.Builder()
//                    .addNetworkInterceptor(
//                            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
//                    .cookieJar(new CookieJarImpl(new PersistentCookieStore(BaseApplication.getInstance())))
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    private Retrofit getRetrofitClient(ProtocolWrapper wrapper) {
        httpBuilder.interceptors().clear();
        httpBuilder.addInterceptor(new BaseInterceptor(wrapper.getHeaders()));
        retrofitBuilder.client(httpBuilder.build())
                .baseUrl(wrapper.getBaseUrl());

        return retrofitBuilder.build();
    }

    //    下载工作的观察者
    private DownSubscriber mDownSubscriber;
    //    是否取消下载
    private boolean isCancelDownload;

    @Override
    @SuppressWarnings("unchecked")
    public <T> void get(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber) {
        getRetrofitClient(protocol).create(BaseApiService.class)
                .executeGet(protocol.getUrl(), protocol.getParams())
                .compose(schedulersTransformer())
                .compose(handleResult())
                .subscribe(subscriber);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void post(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber) {
        getRetrofitClient(protocol).create(BaseApiService.class)
                .executePost(protocol.getUrl(), protocol.getParams())
                .compose(schedulersTransformer())
                .compose(handleResult())
                .subscribe(subscriber);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void json(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber) {
        LogUtil.w("请求入参："+protocol.getParams().toString());
        getRetrofitClient(protocol).create(BaseApiService.class).json(protocol.getUrl(), getJsonBody(protocol))
                .compose(schedulersTransformer())
                .compose(handleResult())
                .subscribe(subscriber);
    }

    /**
     * 先后要发起两次请求
     *
     * @param firstProtocol  第一次请求
     * @param secondProtocol 第二次请求
     * @param subscriber     回调
     * @param <T>            泛型
     */
    @SuppressWarnings("unchecked")
    public <T> void jsonDouble(final ProtocolWrapper firstProtocol, final ProtocolWrapper secondProtocol, Observer<BaseResponse<T>> subscriber) {
        getRetrofitClient(firstProtocol).create(BaseApiService.class).json(firstProtocol.getUrl(), getJsonBody(firstProtocol))
                .compose(schedulersTransformer())
                .flatMap(new Function<BaseResponse, Observable<BaseResponse>>() {

                    @Override
                    public Observable<BaseResponse> apply(@NonNull BaseResponse baseResponseObservable) throws Exception {
                        if (baseResponseObservable.isSuccess()) {
                            return getRetrofitClient(secondProtocol).create(BaseApiService.class)
                                    .json(secondProtocol.getUrl(), getJsonBody(secondProtocol))
                                    .compose(schedulersTransformer())
                                    .compose(handleResult());
                        } else {
                            return Observable.error(new ExceptionHandle.ServerException(baseResponseObservable.getErrMessage()
                                    != null ? baseResponseObservable.getErrMessage() : "", baseResponseObservable.getErrCode()));
                        }

                    }
                })
                .subscribe(subscriber);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void upload(ProtocolWrapper protocol, Observer<BaseResponse<T>> subscriber) {
        getRetrofitClient(protocol).create(BaseApiService.class).upLoadFile(protocol.getUrl(), RequestHelper.composeBody(protocol))
                .compose(schedulersTransformer())
                .compose(handleResult())
                .subscribe(subscriber);
    }

    @SuppressWarnings("unchecked")
    public Disposable download(final ProtocolWrapper protocol, final ProgressCallBack callBack) {
        return getRetrofitClient(protocol).create(BaseApiService.class)
                .downloadFile("bytes=0-", protocol.getUrl())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) {
                        try {
                            RandomAccessFile randomFile = new RandomAccessFile(protocol.getSavePath(), "rw");
                            randomFile.setLength(responseBody.contentLength());
                            long half = responseBody.contentLength() / 2;
                            mDownSubscriber = new DownSubscriber(protocol.getSavePath(), responseBody.contentLength(), callBack);
                            downloadPart(0, half, protocol)
                                    .mergeWith(downloadPart(half, responseBody.contentLength(), protocol))
                                    .subscribe(mDownSubscriber);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ExceptionHandle.RespondThrowable respondThrowable = ExceptionHandle.handleException(throwable);
                        if (!isCancelDownload) {
                            callBack.onError(respondThrowable);
                        }
                    }
                });
    }

    /**
     * 分步下载,并将其转为键值对,方便区分是哪个线程
     *
     * @param start    开始位置
     * @param end      结束位置
     * @param protocol 端口
     * @return 结果
     */
    private Observable<Integer> downloadPart(@NonNull final long start, @NonNull final long end, final ProtocolWrapper protocol) {
        return getRetrofitClient(protocol).create(BaseApiService.class).downloadFile("bytes=" + start + "-" + end, protocol.getUrl())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .flatMap(new Function<ResponseBody, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull final ResponseBody responseBody) throws Exception {
                        return downloadFlatMap(start, responseBody, protocol);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param start        写入文件起始位置
     * @param responseBody 请求返回
     * @param protocol     接口封装
     * @return 被观察者
     */
    private Observable<Integer> downloadFlatMap(final long start, final ResponseBody responseBody, final ProtocolWrapper protocol) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                try {
                    RandomAccessFile randomFile = new RandomAccessFile(protocol.getSavePath(), "rw");
                    randomFile.seek(start);
                    InputStream in = responseBody.byteStream();
                    byte[] buffer = new byte[2048];
                    int read;
                    while (((read = in.read(buffer)) != -1)) {
                        randomFile.write(buffer, 0, read);
                        emitter.onNext(read);
                        if (isCancelDownload) {
                            randomFile.close();
                            return;
                        }
                    }
                    randomFile.close();
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * 取消下载
     */
    public void cancelDownload() {
        isCancelDownload = true;
    }

    private ObservableTransformer schedulersTransformer() {
        return new ObservableTransformer() {

            @Override
            public ObservableSource apply(@NonNull Observable upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 对结果进行预处理
     *
     * @param <T> 数据
     * @return 转换后的结果
     */
    private <T> ObservableTransformer<BaseResponse<T>, BaseResponse<T>> handleResult() {
        return new ObservableTransformer<BaseResponse<T>, BaseResponse<T>>() {
            @Override
            public Observable<BaseResponse<T>> apply(@NonNull Observable<BaseResponse<T>> upstream) {
                return upstream.flatMap(new Function<BaseResponse<T>, Observable<BaseResponse<T>>>() {
                    @Override
                    public Observable<BaseResponse<T>> apply(@NonNull BaseResponse<T> tBaseResponse) throws Exception {
                        if (tBaseResponse.isSuccess()) {
                            return createData(tBaseResponse);
                        } else {
                            return Observable.error(new ExceptionHandle.ServerException(tBaseResponse.getErrMessage() != null ? tBaseResponse.getErrMessage() : "", tBaseResponse.getErrCode()));
                        }
                    }
                }).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private RequestBody getJsonBody(ProtocolWrapper protocol) {
        String json = "";
        try {
            json = RequestHelper.composeJson(protocol.getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        return RequestBody.create(JSON, json);
    }

    /**
     * 创建成功的数据
     *
     * @param data 数据
     * @param <T>  泛型
     * @return 结果
     */
    private <T> Observable<T> createData(final T data) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> emitter) throws Exception {
                try {
                    emitter.onNext(data);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
    }

//    public class ResultFunc<T> implements Function<String, Result<T>> {
//        Class beanClass;
//
//        public ResultFunc(Class beanClass) {
//            this.beanClass = beanClass;
//        }
//
//        @Override
//        public Result<T> apply(String result) {
//            Result<T> t = null;
//            try {
//                t = (Result<T>) fromJsonObject(result, beanClass);
//            } catch (JsonSyntaxException e) {//解析异常，说明是array数组
//
//            }
//            return t;
//        }
//
//        public <T> Result<T> fromJsonObject(String reader, Class<T> clazz) {
//            Type type = new ParameterizedTypeImpl(Result.class, new Class[]{clazz});
//            return new Gson().fromJson(reader, type);
//        }
//    }

}
