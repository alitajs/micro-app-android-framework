package com.whalecloud.minialita.net.interior;

/**
 * Created by TQ on 2018/6/5.
 */

public interface HttpErrorCode {

    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int REQUEST_TIMEOUT = 408;
    int INTERNAL_SERVER_ERROR = 500;
    int BAD_GATEWAY = 502;
    int SERVICE_UNAVAILABLE = 503;
    int GATEWAY_TIMEOUT = 504;
}
