package com.liu.wanxinp2p.api.depository.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 返回参数信息 - 基类
 */
@Data
@ApiModel(value = "DepositoryBaseResponse" , description = "返回参数信息的基类")
public class DepositoryBaseResponse {

    @ApiModelProperty("返回码")
    private String respCode;
    @ApiModelProperty("描述信息")
    private String respMsg;
    @ApiModelProperty("交易状态")
    private Integer status;
    @ApiModelProperty("请求流水号")
    private String requestNo;
}
