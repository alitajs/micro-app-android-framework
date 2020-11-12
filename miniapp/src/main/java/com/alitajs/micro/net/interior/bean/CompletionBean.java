package com.alitajs.micro.net.interior.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class CompletionBean {

    //status,message,responseData
    int status;//0 成功， 1 没有此插件， 2 没有方法， 3 错误
    String message;
    Object responseData;

    public CompletionBean(int status, String message, Object responseData) {
        this.status = status;
        this.message = message;
        this.responseData = responseData;
    }

    public JSONObject getResult(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", status);
            jsonObject.put("message", message);
            jsonObject.put("responseData", responseData);
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            return jsonObject;
        }
    }
}
