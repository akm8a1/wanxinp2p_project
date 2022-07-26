package com.liu.wanxinp2p.api.consumer.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 用户充值请求信息
 */
@Data
@Accessors(chain = true)
@ApiModel("用户充值请求信息")
public class RechargeRequest {

    @ApiModelProperty("标识")
    private Long id;

    @ApiModelProperty("用户编号")
    private String userNo;

    @ApiModelProperty("请求流水号")
    private String requestNo;

    @ApiModelProperty("充值金额")
    private BigDecimal amount;

    @ApiModelProperty("页面回调URL")
    private String callbackUrl;
}