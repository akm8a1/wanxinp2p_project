package com.liu.wanxinp2p.account.controller;

import com.liu.wanxinp2p.account.service.AccountService;
import com.liu.wanxinp2p.api.account.AccountAPI;
import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountLoginDTO;
import com.liu.wanxinp2p.api.account.model.AccountRegisterDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "AccountController",description = "统一账户服务Controller")
@RestController
public class AccountController implements AccountAPI {
    @Autowired
    private AccountService accountService;

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "mobile" , value = "手机号码" , dataType = "String" , required = true)
    @GetMapping("/sms/{mobile}")
    @Override
    public RestResponse getSMSCode(@PathVariable("mobile") String mobile) {
        return accountService.getSMSCode(mobile);
    }

    @ApiOperation("校验验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name="mobile",value="手机号码",dataType = "String"),
            @ApiImplicitParam(name="key",value = "校验码key", dataType = "String"),
            @ApiImplicitParam(name="code",value="验证码",dataType = "String")
    })
    @GetMapping("/mobiles/{mobile}/key/{key}/code/{code}")
    @Override
    public RestResponse<Integer> checkMobile(@PathVariable("mobile")String mobile,
                                             @PathVariable("key")String key,
                                             @PathVariable("code")String code) {
        return RestResponse.success(accountService.checkMobile(mobile,key,code));
    }


    @ApiOperation("用户注册")
    @ApiImplicitParam(name = "accountRegisterDTO", value="账户注册信息", required = true,
                    dataType = "AccountRegisterDTO", paramType = "body")
    @Override
    @PostMapping("/l/accounts")
    public RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO) {
        return RestResponse.success(accountService.register(accountRegisterDTO));
    }

    @ApiOperation(value = "用户登录",httpMethod = "POST")
    @ApiImplicitParam(name="accountLoginDTO",value="登录信息",required = true,dataType = "AccountLoginDTO",paramType = "body")
    @PostMapping("/l/accounts/session")
    @Override
    public RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO) {
        return RestResponse.success(accountService.login(accountLoginDTO));
    }

}
