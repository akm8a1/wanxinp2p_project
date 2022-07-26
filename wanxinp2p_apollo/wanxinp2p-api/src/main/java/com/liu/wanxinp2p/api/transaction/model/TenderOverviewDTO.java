package com.liu.wanxinp2p.api.transaction.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标信息预览
 */
@ApiModel(value = "TenderOverviewDTO", description = "投标信息预览")
@Data
public class TenderOverviewDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("投标人用户标识")
    private Long consumerId;

    @ApiModelProperty("投标人用户名")
    private String consumerUsername;

    @ApiModelProperty("投标冻结金额")
    private BigDecimal amount;

    @ApiModelProperty("投标方式")
    private String tenderWay = "手动出借";

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;
}
