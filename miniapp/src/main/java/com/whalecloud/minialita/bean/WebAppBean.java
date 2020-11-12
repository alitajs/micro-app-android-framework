package com.whalecloud.minialita.bean;

import java.util.ArrayList;

public class WebAppBean {

    public ArrayList<WebAppData> records;

    public class WebAppData {
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
