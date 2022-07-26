package com.liu.wanxinp2p.common.exception;


import com.liu.wanxinp2p.common.exception.ErrorCode;

/**
 * 用户错误码
 */
public enum AccountErrorCode implements ErrorCode {
    /*用户名已存在*/
    ERROR_USER_EXISTED(130101,"用户名已存在"),
    /*用户名未注册*/
    ERROR_USER_UNREGISTERED(130104,"用户未注册"),
    /*用户名或密码错误*/
    ERROR_USER_INFO(130105,"用户名或密码错误"),
    /*注册失败*/
    ERROR_FAIL_REGISTER(140141,"注册失败"),
    /*获取短信验证码失败*/
    ERROR_FAIL_SMS(140151,"获取短信验证码失败"),
    /*短信验证码错误*/
    ERROR_FAIL_SMSVERIFY(140152,"验证码错误");
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

    private AccountErrorCode(int code,String desc) {
        this.code = code;
        this.desc = desc;
    }
}
