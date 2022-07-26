package com.liu.wanxinp2p.api.consumer.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BorrowDTO", description = "借款人用户信息")
public class BorrowDTO {
    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String fullname;

    @ApiModelProperty("身份证号")
    private String idNumber;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("生日")
    private String birthday;

    @ApiModelProperty("性别")
    private String gender;
}
