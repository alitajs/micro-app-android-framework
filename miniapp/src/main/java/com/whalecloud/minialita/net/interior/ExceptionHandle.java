package com.whalecloud.minialita.net.interior;

import android.content.Intent;


import java.net.ConnectException;

import retrofit2.adapter.rxjava2.HttpException;


public class ExceptionHandle {

    public static RespondThrowable handleException(Throwable e) {
        RespondThrowable ex;
        if (e instanceof HttpException) {
            ex = new RespondThrowable(e, ErrorCode.CODE_HTTP_ERROR+"");
           // ex.message = BaseApplication.getInstance().getString(R.string.common_request_error_tip);
            return ex;
        } else if (e instanceof ServerException) {
            ServerException resultException = (ServerException) e;
            ex = new RespondThrowable(resultException, resultException.code);
            ex.message = resultException.message;
            Intent intent;
//            switch (resultException.code){
//                case ErrorCode.CODE_TOKEN_ERROR:
//                    intent = new Intent("xxf.gemini.login");
//                    intent.putExtra("isTokenError", true);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    BaseApplication.getInstance().startActivity(intent);
//                    //ToastUtil.showNetErrorToast(BaseApplication.getContext().getString(R.string.token_error_tip));
//                    break;
//            }
            return ex;
        } else if (e instanceof ConnectException) {
            ConnectException resultException = (ConnectException) e;
            ex = new RespondThrowable(resultException, ErrorCode.CODE_NET_ERROR+"");
            //ex.message = BaseApplication.getInstance().getString(R.string.common_network_error_tip);
            return ex;
        } else {
            ex = new RespondThrowable(e, ErrorCode.CODE_UNKNOWN+"");
            //ex.message = BaseApplication.getInstance().getString(R.string.common_request_error_tip);
            return ex;
        }
    }

    public static class RespondThrowable extends Exception {
        public String code;
        public String message;
        public Throwable throwable;

        public RespondThrowable(Throwable throwable, String code) {
            super(throwable);
            this.throwable = throwable;
            this.code = code;
            this.message = throwable.getMessage();
        }
    }

    public static class ServerException extends RuntimeException {

        public String code;
        public String message;

        public ServerException(String message, String code) {
            super(message);
            this.code = code;
            this.message = message;
        }
    }

}

