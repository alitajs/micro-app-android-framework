package com.whalecloud.minialita.net.interior;

import android.text.TextUtils;

import com.whalecloud.minialita.net.protocol.ProtocolWrapper;

import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RequestHelper {
    /**
     * 构建一个带有参数的URL
     *
     * @param url    URL
     * @param params 参数
     * @return
     * @throws Exception
     */
    public static String composeUrl(String url, Map<String, String> params) throws Exception {
        Iterator<String> iterator = params.keySet().iterator();
        StringBuilder buffer = new StringBuilder();
        while (iterator.hasNext()) {
            String name = iterator.next();
            String value = params.get(name);
            value = URLEncoder.encode(value, "utf-8");
            if (!TextUtils.isEmpty(value)) {
                buffer.append("&").append(name).append("=").append(value);
            }
        }

        String param = buffer.toString();
        if (TextUtils.isEmpty(param)) {
            return url;
        }

        if (url.contains("?")) {
            return url + buffer.toString();
        }

        if (param.startsWith("&")) {
            buffer.deleteCharAt(0);
        }

        return url + "?" + buffer.toString();
    }

    /**
     * 构造带参数的包体
     * @param params
     * @return
     */
    public static RequestBody composeBody(Map<String, String> params){
        FormBody.Builder bodyBuild = new FormBody.Builder();
        Iterator<String> iterator = params.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            String value = params.get(name);
            bodyBuild.add(name,value);
        }
        return bodyBuild.build();
    }

    /**
     * 构造带参数上传文件包体
     * @param protocol 接口
     * @return 包体
     */
    public static RequestBody composeBody(ProtocolWrapper protocol){
        MultipartBody.Builder requestBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (String paramName : protocol.getParams().keySet()) {
            String value = (String) protocol.getParams().get(paramName);
            requestBuilder.addFormDataPart(paramName, value);
        }

        for (String listName:protocol.getFileMap().keySet()){
            List<File> fileList = protocol.getFileMap().get(listName);
            for (File file: fileList){
                requestBuilder.addFormDataPart(listName, file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            }
        }
        return requestBuilder.build();
    }

    /**
     * 构造json参数串
     * @param params
     * @return
     * @throws Exception
     */
    public static String composeJson(Map<String, Object> params) throws Exception {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            jsonObject.put(name,value);

        }
        return jsonObject.toString();
    }

    public static RequestBody composeJsonBody(ProtocolWrapper protocol){
        String json = "";
        try {
            json = RequestHelper.composeJson(protocol.getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        return RequestBody.create(JSON, json);
    }
}
