package com.liu.wanxinp2p.account.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "测试用Controller",description = "测试")
@RestController
public class HelloController {
    @ApiOperation(value = "hello")
    @GetMapping("/hello")
    public String hello() {
        return "hello account-service";
    }
}
