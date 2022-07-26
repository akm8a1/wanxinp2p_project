package com.liu.wanxinp2p.common.domain;

/**
 * 状态描述
 */
public enum StatusCode {
    /*
    * 发布/同步失败
    * */
    STATUS_FAIL(2,"发布/同步失败"),
    /*
    * 已经发布/同步/绑定
    * */
    STATUS_IN(1,"已发布/同步"),
    /*
    * 未发布
    * */
    STATUS_OUT(0,"未发布/同步");

    private Integer code;

    private String desc;

    private StatusCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
