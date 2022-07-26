package com.liu.wanxinp2p.common.exception;

public interface ErrorCode {
    /**
     * 获得异常码
     * @return
     */
    int getCode();

    /**
     * 获得错误提示信息
     * @return
     */
    String getDesc();
}
