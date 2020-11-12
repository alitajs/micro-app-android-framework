package com.whalecloud.minialita.net.interior.convert;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.whalecloud.minialita.net.interior.BaseResponse;
import com.whalecloud.minialita.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by TQ on 2018/5/30.
 */

final class CustomGsonResponseBodyConverter implements Converter<ResponseBody, BaseResponse> {
    private final Gson gson;
    private final TypeAdapter adapter;

    CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<BaseResponse> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override public BaseResponse convert(ResponseBody value) throws IOException {
        try {
            String json = value.string();
            JSONObject jsonObject = new JSONObject(json);
            LogUtil.w("请求返回结果："+json);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setErrCode(jsonObject.optString("resultCode"));
            baseResponse.setErrMessage(jsonObject.optString("resultMsg"));
            baseResponse.setSuccess(jsonObject.optInt("resultCode") == 0);
            baseResponse.setData(jsonObject.optString("resultData"));
            return baseResponse;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
