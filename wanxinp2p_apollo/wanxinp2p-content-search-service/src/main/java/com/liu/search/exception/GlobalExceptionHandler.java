package com.liu.search.exception;

import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.common.exception.CommonErrorCode;
import com.liu.wanxinp2p.common.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 截取所有的Exception异常弹出
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RestResponse exceptionHandler(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Exception e) {
        if (e instanceof GlobalException) {
            GlobalException globalException = (GlobalException) e;
            return new RestResponse(globalException.getErrorCode().getCode(),globalException.getErrorCode().getDesc());
        } else if (e instanceof NoHandlerFoundException) {
            return new RestResponse(404,"找不到对应资源");
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            return new RestResponse(405,"method方法不支持");
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            return new RestResponse(415,"不支持媒体类型");
        }
        log.error("系统异常:"+e.getMessage());
        return new RestResponse(CommonErrorCode.ERROR_UNKNOWN.getCode(), CommonErrorCode.ERROR_UNKNOWN.getDesc());
    }
}