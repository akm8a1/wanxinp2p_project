package com.liu.wanxinp2p.api.account.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="AccountRegisterDTO",description = "账户注册信息")
public class AccountRegisterDTO {

    @ApiModelProperty(value="用户名" , required = true)
    private String username;

    @ApiModelProperty(value="手机号" , required = true)
    private String mobile;

    @ApiModelProperty(value="密码" , required = true)
    private String password;
}
