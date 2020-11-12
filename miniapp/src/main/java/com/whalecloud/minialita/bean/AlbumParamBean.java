package com.whalecloud.minialita.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AlbumParamBean {

    public int count = 9;
    public ArrayList<String> sizeType = new ArrayList<>();
    public ArrayList<String> sourceType = new ArrayList<>();
    public boolean base64;

    public AlbumParamBean(String param) {
        try {
            JSONObject jsonObject = new JSONObject(param);
            count = jsonObject.optInt("count", 9);
            base64 = jsonObject.optBoolean("base64", true);
            JSONArray sizeTypes = jsonObject.optJSONArray("sizeType");
            if (sizeTypes == null){
                sizeType.add("compressed");
            }else {
                for (int i = 0; i < sizeTypes.length(); i ++){
                    String s = sizeTypes.getString(i);
                    sizeType.add(s);
                }
                //为空则设置默认值
                if (sizeType.size() == 0){
                    sizeType.add("compressed");
                }
            }

            JSONArray sourceTypes = jsonObject.optJSONArray("sourceType");
            if (sourceTypes != null){
                for (int i = 0; i < sourceTypes.length(); i ++){
                    String s = sourceTypes.getString(i);
                    sourceType.add(s);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            count = 9;
            base64 = true;
            sizeType.add("compressed");
        }
    }
}
