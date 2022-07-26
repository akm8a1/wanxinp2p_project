package com.liu.wanxinp2p.transaction.common.constant;

/**
 * 交易状态枚举类
 */
public enum TradingCode {
    /**
     * 已冻结
     */
    FROZEN("FROZEN", "已冻结"),
    /**
     * 已放款
     */
    LOAN("LOAN", "已放款"),
    /**
     * 已退款
     */
    REFUNDED("REFUNDED", "已退款");


    private String code;
    private String desc;

    TradingCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}