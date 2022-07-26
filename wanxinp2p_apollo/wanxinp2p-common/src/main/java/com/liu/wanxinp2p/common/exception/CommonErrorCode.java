package com.liu.wanxinp2p.common.exception;

/**
 * 提供通用的错误码
 */
public enum CommonErrorCode implements ErrorCode {

    /*通用异常编码*/
    SUCCESS(0,"成功"),
    /*传入参数与接口不匹配*/
    ERROR_NOT_MATCH(100101,"传入参数与接口不匹配"),
    /*验证码错误*/
    ERROR_NOT_VERIFY(100102,"验证码错误"),
    /*验证码为空*/
    ERROR_EMPTY_VERIFY(100103,"验证码为空"),
    /*查询结果为空*/
    ERROR_EMPTY_QUERY(100104,"查询结果为空"),
    /*ID格式不正确或超出了Long的存储范围*/
    ERROR_FORMAT_ID(100105,"ID格式不正确或超出了Long的存储范围"),
    /*请求失败*/
    ERROR_REQUEST(100106,"请求失败"),
    /*未知错误*/
    ERROR_UNKNOWN(500000,"未知错误");
    private int code;

    private String desc;

    private CommonErrorCode(int code,String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getDesc() {
        return null;
    }
}
