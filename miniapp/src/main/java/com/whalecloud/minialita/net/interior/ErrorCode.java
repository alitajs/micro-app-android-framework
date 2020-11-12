package com.whalecloud.minialita.net.interior;

/**
 * Created by TQ on 2018/6/5.
 */

public interface ErrorCode {
    /**
     * 未知错误
     */
    int CODE_UNKNOWN = -1;
    /**
     * 成功
     */
    int CODE_OK = 0;
    /**
     * 操作失败
     */
    int CODE_ERROR = 1;

    /**
     * token失效
     */
    int CODE_TOKEN_ERROR = 13;

    /**
     * 网络连接失败
     */
    int CODE_NET_ERROR = 1001;

    /**
     * HTTP协议错误
     */
    int CODE_HTTP_ERROR = 1002;

    /**
     * 解析错误
     */
    int CODE_PARSE_ERROR = 1003;

    /**
     * 网络错误
     */
   int CODE_NETWORD_ERROR = 1004;

    /**
     * 证书出错
     */
    int CODE_SSL_ERROR = 1005;

    /**
     * 连接超时
     */
    int CODE_TIMEOUT_ERROR = 1006;

    /**
     * 自定义错误
     */
    int CODE_CUSTOM_ERROR = 10086;

}
