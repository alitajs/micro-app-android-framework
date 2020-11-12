package com.whalecloud.minialita.net.interior;


import com.whalecloud.minialita.net.interior.gson.MGson;

/**
 * 网络返回基类 支持泛型
 */
public class BaseResponse<T> {

    private String errCode;
    private boolean success;
    private String errMessage;
    private T resultData;
    private String data;

    public BaseResponse() {
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public T getData(Class<T> c) {
        if (resultData == null) {
            resultData = MGson.newGson().fromJson(data, c);
        }
        return resultData;
    }
//    public T getData() {
//        return resultData;
//    }

    public String getDataJson() {
        return data;
    }

    public void setData(String json) {
        this.data = json;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
