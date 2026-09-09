package com.ywj.live.web.error;

public class YwjErrorException extends RuntimeException{

    private int errorCode;
    private String errorMsg;

    public YwjErrorException(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public YwjErrorException(BaseError baseError) {
        this.errorCode = baseError.getErrorCode();
        this.errorMsg = baseError.getErrorMsg();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}