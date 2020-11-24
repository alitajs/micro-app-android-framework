package com.alitajs.micro.bean;

import java.util.ArrayList;

public class MicorAppBean {

    public ArrayList<MicorAppData> records;

    public static class MicorAppData {
        public String sourceFrom;
        public int pageNo;
        public int pageSize;
        public String sortName;
        public String sortOrder;
        public String id;
        public String appid;
        public String appsecret;
        public String appName;
        public String appDesc;
        public long createTime;
        public long updateTime;
        public String status;
        public String isDeleted;
        public String remarks;
        public String appIconUrl;
        public String belongToUser;
        public String belongToApp;
        public String opration;
        public String versionId;
    }
}
