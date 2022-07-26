package com.liu.wanxinp2p.common.exception;


/**
 * 异常编码 0成功、-1熔断、 -2 标准参数校验不通过 -3会话超时
 * 前两位:服务标识
 * 中间两位:模块标识
 * 后两位:异常标识
 * c端用户服务异常编码以14开始
 */
public enum ConsumerErrorCode implements ErrorCode {
    ////////////////////////////////////c端用户服务异常编码 //////////////////////////
    /**
     * 不存在的用户信息
     */
    ERROR_NO_USER(140101,"不存在的用户信息"),
    ERROR_FAIL_REQUEST(140102,"请求失败"),
    ERROR_HAVED_ACCOUNT(140105,"用户已开户"),
    ERROR_FAIL_REGISTER(140106,"注册失败"),
    ERROR_HAVED_USER(140107, "用户已存在"),
    ERROR_INFO_USER(140108, "身份信息不一致"),

    ERROR_FAIL_PAY(140131,"用户充值失败"),
    ERROR_FAIL_ACCOUNT(140132,"用户存管账户未开通成功"),


    ERROR_FAIL_GETMONEY(140141,"用户提现失败"),


    ERROR_LOCK_CARD(140151,"银行卡已被绑定"),
    ;

    private int code;

    private String desc;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    private ConsumerErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static ConsumerErrorCode setErrorCode(int code) {
        for (ConsumerErrorCode errorCode : ConsumerErrorCode.values()) {
            if (errorCode.getCode()==code) {
                return errorCode;
            }
        }
        return null;
    }
}
