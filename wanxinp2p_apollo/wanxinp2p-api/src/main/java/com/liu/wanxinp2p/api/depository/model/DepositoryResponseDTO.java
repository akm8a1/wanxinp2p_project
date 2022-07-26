package com.liu.wanxinp2p.api.depository.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 银行存管系统返回的str,转换的JSON对象
 */
@Data
@ApiModel(value = "DepositoryResponseDTO", description = "银行存管系统返回的str,转换的JSON对象")
public class DepositoryResponseDTO<T> implements Serializable {

    @ApiModelProperty("业务数据报文，JSON格式")
    private T respData;

    @ApiModelProperty("签名数据")
    private String signature;
}
