package com.liu.wanxinp2p.api.transaction.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "ProjectDTO", description = "标的DTO")
public class ProjectDTO {

    @ApiModelProperty("主键")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("发标人用户标识")
    private Long consumerId;

    @ApiModelProperty("发标人用户编码")
    private String userNo;

    @ApiModelProperty("标的编码")
    private String projectNo;

    @ApiModelProperty("标的名称")
    private String name;

    @ApiModelProperty("标的描述")
    private String description;

    @ApiModelProperty("标的类型")
    private String type;

    @ApiModelProperty("标的期限(单位:天)")
    private Integer period;

    @ApiModelProperty("年化利率(投资人视图)")
    private BigDecimal annualRate;

    @ApiModelProperty("年化利率(借款人视图)")
    private BigDecimal borrowerAnnualRate;

    @ApiModelProperty("年化利率(平台佣金，利差)")
    private BigDecimal commissionAnnualRate;

    @ApiModelProperty("还款方式5.4.1")
    private String repaymentWay;

    @ApiModelProperty("募集金额")
    private BigDecimal amount;

    @ApiModelProperty("标的状态")
    private String projectStatus;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("可用状态")
    private Integer status;

    @ApiModelProperty("是否是债权出让标")
    private Integer isAssignment;

    @ApiModelProperty("请求流水号")
    private String requestNo;

    @ApiModelProperty("剩余额度")
    private BigDecimal remainingAmount;

    @ApiModelProperty("风险等级, 目前默认B")
    private String risk = "B";

    @ApiModelProperty("出借人数")
    private Integer tenderCount;

}
