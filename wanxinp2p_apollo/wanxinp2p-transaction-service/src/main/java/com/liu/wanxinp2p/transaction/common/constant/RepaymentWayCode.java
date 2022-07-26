package com.liu.wanxinp2p.transaction.common.constant;

/**
 * 还款方式编码
 */
public enum RepaymentWayCode {
    /**
     * 一次性还款（含本息）
     */
    ALL("ALL", "一次性还款（含本息）"),

    /**
     * 先息后本: 贷款下款之后，先支付利息，然后按还款约定支付本金
     */
    INTEREST_FIRST("INTEREST_FIRST", "先息后本"),

    /**
     * 等额本息: 在还款期内，每月偿还同等数额的贷款(包括本金和利息)
     */
    FIXED_REPAYMENT("FIXED_REPAYMENT", "等额本息"),

    /**
     * 等额本金:在还款期内把贷款数总额等分，每月偿还同等数额的本金和剩余贷款在该月所产生的利息
     */
    FIXED_CAPITAL("FIXED_CAPITAL", "等额本金");

    private String code;
    private String desc;

    RepaymentWayCode(String code, String desc) {
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
