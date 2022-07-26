package com.liu.wanxinp2p.common.exception;

/**
 * 自定义异常类
 */
public class GlobalException extends RuntimeException{

    private static final long serialVersionUID = 5565760508056698922L;
    /*错误码*/
    private ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public GlobalException() {
        super();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
