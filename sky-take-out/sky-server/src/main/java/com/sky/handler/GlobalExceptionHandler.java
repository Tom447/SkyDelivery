package com.sky.handler;

import com.aliyuncs.utils.StringUtils;
import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Duplication;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result baseExceptionHandler(BaseException ex){
    	ex.printStackTrace();
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获DuplicateKeyException，该异常会在数据库字段（唯一）重复时抛出
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result baseExceptionHandler(DuplicateKeyException ex){
        ex.printStackTrace();
        log.error("异常信息：{}", ex.getMessage());
        String errorMessage = MessageConstant.UNKNOWN_ERROR;
        String message = ex.getCause().getMessage();
        if (!StringUtils.isEmpty(message)){
            String[] msgs = message.split(" ");
            errorMessage = msgs[2] + "已存在";
        }
        return Result.error(errorMessage);
    }

    /**
     * 捕获其他异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception ex){
    	ex.printStackTrace();
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
