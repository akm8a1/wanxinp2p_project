package com.liu.wanxinp2p.api.account.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="AccountDTO",description = "账户信息")
public class AccountDTO {
    @ApiModelProperty(value = "标识",required = true)
    private Long id;

    @ApiModelProperty(value = "用户名",required = true)
    private String username;

    @ApiModelProperty(value = "手机号码",required = true)
    private String mobile;

    @ApiModelProperty(value = "账号状态" , required = true)
    private Integer status;

    @ApiModelProperty(value="域，c:c端用户,b:b端用户",required = true)
    private String domain;
}
