package com.sky.exception;

/**
 * 业务异常
 */
public class BusinessException extends BaseException{
    public BusinessException() {
    }

    public BusinessException(String msg) {
        super(msg);
    }
}
