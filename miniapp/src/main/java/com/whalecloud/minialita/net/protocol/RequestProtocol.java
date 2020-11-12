package com.whalecloud.minialita.net.protocol;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 服务端基本请求协议
 */
public class RequestProtocol implements ProtocolWrapper {

    private TreeMap<String, Object> mParams;
    private HashMap<String, String> mHeader;
    private TreeMap<String, List<File>> mFiles;
    private String mBaseUrl = "http://47.92.108.46:8009/";
    private String mRealUrl;
    //    下载地址,带后缀完整路径
    private String mSavePath;

    public RequestProtocol(String region) {
        this.mParams = new TreeMap<>();
        this.mHeader = new HashMap<>();
        this.mFiles = new TreeMap<>();

        this.mRealUrl = region;
    }

    @Override
    public String getBaseUrl() {
        return this.mBaseUrl;
    }

    @Override
    public String getUrl() {
        return this.mRealUrl;
    }

    @Override
    public String getSavePath() {
        return mSavePath;
    }

    /**
     * 返回URL参数
     *
     * @return
     */
    @Override
    public Map<String, Object> getParams() {
        return this.mParams;
    }

    /**
     * 返回HTTP头
     *
     * @return
     */
    @Override
    public Map<String, String> getHeaders() {
        return this.mHeader;
    }

    @Override
    public Map<String, List<File>> getFileMap() {
        return this.mFiles;
    }

    public void putHead(String key, String value) {
        mHeader.put(key, value);
    }

    /**
     * 添加URL参数
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        this.mParams.put(key, value);
    }

    public void putFile(String key, List<File> file) {
        this.mFiles.put(key, file);
    }

    public void putSavePath(String path){
        mSavePath = path;
    }

    /**
     * 构建协议
     */
    public RequestProtocol build() {
        return this;
    }

}
