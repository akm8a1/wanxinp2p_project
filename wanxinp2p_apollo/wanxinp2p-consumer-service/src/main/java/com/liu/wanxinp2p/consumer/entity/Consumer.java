package com.liu.wanxinp2p.consumer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("consumer")
public class Consumer implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
    * 主键
    * */
    @TableId("ID")
    private Long id;

    /**
     * 用户名
     */
    @TableField("USERNAME")
    private String username;

    /**
     * 真实姓名
     */
    @TableField("FULLNAME")
    private String fullname;

    /**
     * 身份证号码
     */
    @TableField("ID_NUMBER")
    private String idNumber;

    /**
     * 用户编码,生成唯一,用户在存管系统标识
     */
    @TableField("USER_NO")
    private String userNo;

    /**
     * 平台预留手机号
     */
    @TableField("MOBILE")
    private String mobile;

    /**
     * 用户类型,个人还是企业
     */
    @TableField("USER_TYPE")
    private String userType;

    /**
     * 用户角色，借款人还是投资人
     */
    @TableField("ROLE")
    private String role;

    /**
     * 存管授权列表
     */
    @TableField("AUTH_LIST")
    private String authList;

    /**
     * 是否已经绑定了银行卡
     */
    @TableField("IS_BIND_CARD")
    private Integer isBindCard;

    /**
     * 可用状态
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 可贷款额度
     */
    @TableField("LOAN_AMOUNT")
    private BigDecimal loanAmount;

    /**
     * 请求的流水号
     */
    @TableField("REQUEST_NO")
    private String requestNo;
}
