package com.liu.wanxinp2p.api.depository.model;

public enum DepositoryReturnCode {

    RETURN_CODE_00000("00000","成功"),
    RETURN_CODE_00001("00001","系统异常"),
    RETURN_CODE_00002("00002","系统内部错误"),
    RETURN_CODE_00003("00003","参数校验不通过"),
    RETURN_CODE_00004("00004","签名验证失败"),
    ;

    private String code;

    private String desc;

    private DepositoryReturnCode(String code,String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
