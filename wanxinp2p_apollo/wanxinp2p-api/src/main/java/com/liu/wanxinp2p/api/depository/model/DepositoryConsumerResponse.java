package com.liu.wanxinp2p.api.depository.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 开户返回参数信息
 */
@Data
@ApiModel(value = "DepositoryConsumerResponse" , description = "开户返回参数信息")
public class DepositoryConsumerResponse extends DepositoryBaseResponse{

    @ApiModelProperty("银行代码")
    private String bankCode;

    @ApiModelProperty("银行名称")
    private String bankName;
}
