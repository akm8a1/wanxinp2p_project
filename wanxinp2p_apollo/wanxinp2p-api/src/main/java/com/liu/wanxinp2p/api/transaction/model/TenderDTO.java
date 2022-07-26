package com.liu.wanxinp2p.api.transaction.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标信息表
 */
@Data
@ApiModel(value = "TenderDTO", description = "投标信息表")
public class TenderDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("投标人用户标识")
    private Long consumerId;

    @ApiModelProperty("投标人用户名")
    private String consumerUsername;

    @ApiModelProperty("投标人用户编码")
    private String userNo;

    @ApiModelProperty("标的标识")
    private Long projectId;

    @ApiModelProperty("标的编码")
    private String projectNo;

    @ApiModelProperty("投标冻结金额")
    private BigDecimal amount;

    @ApiModelProperty("投标状态")
    private String tenderStatus;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("投标/债权转让 请求流水号")
    private String requestNo;

    @ApiModelProperty("可用状态")
    private Integer status;

    @ApiModelProperty("标的名称")
    private String projectName;

    @ApiModelProperty("标的期限(单位:天) -- 冗余字段")
    private Integer projectPeriod;

    @ApiModelProperty("年化利率(投资人视图) -- 冗余字段")
    private BigDecimal projectAnnualRate;

    @ApiModelProperty("标的信息")
    private ProjectDTO project;

    @ApiModelProperty("预期收益")
    private BigDecimal expectedIncome;
}
