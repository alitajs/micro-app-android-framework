package com.whalecloud.minialita.net.interior;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 *  网络请求的API接口
 * Created by TQ on 2018/5/4.
 */

public interface BaseApiService {

    @GET()
    Observable<BaseResponse> executeGet(
            @Url String url,
            @QueryMap Map<String, Object> maps);

    @POST()
    @FormUrlEncoded
    Observable<BaseResponse> executePost(
            @Url String url,
            @FieldMap Map<String, Object> maps);

    @POST()
    Observable<BaseResponse> json(
            @Url String url,
            @Body RequestBody jsonStr);


    @POST()
    Observable<BaseResponse> upLoadFile(
            @Url String url,
            @Body() RequestBody requestBody);

    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Header("RANGE") String downParam, @Url String fileUrl);
}
