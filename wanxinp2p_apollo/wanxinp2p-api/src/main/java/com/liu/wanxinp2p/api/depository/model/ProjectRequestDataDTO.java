package com.liu.wanxinp2p.api.depository.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 银行存管系统创建标的的reqData参数实体
 */
@Data
@ApiModel(value = "ProjectRequestDataDTO", description = "银行存管系统创建标的的reqData参数实体")
public class ProjectRequestDataDTO {

    @ApiModelProperty("请求流水号")
    private String requestNo;

    @ApiModelProperty("平台用户编号")
    private String userNo;

    @ApiModelProperty("标的号")
    private String projectNo;

    @ApiModelProperty("标的金额")
    private BigDecimal projectAmount;

    @ApiModelProperty("标的名称")
    private String projectName;

    @ApiModelProperty("标的描述")
    private String projectDesc;

    @ApiModelProperty("标的产品类型")
    private String projectType;

    @ApiModelProperty("标的期限")
    private Integer projectPeriod;

    @ApiModelProperty("年化利率( 5%只传5 )")
    private BigDecimal annualRate;

    @ApiModelProperty("还款方式")
    private String repaymentWay;

    @ApiModelProperty("是否是债权出让标")
    private Integer isAssignment;
}
