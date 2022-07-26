package com.liu.wanxinp2p.consumer.controller;

import com.alibaba.fastjson.JSON;
import com.liu.wanxinp2p.api.account.model.LoginUser;
import com.liu.wanxinp2p.api.consumer.ConsumerAPI;
import com.liu.wanxinp2p.api.consumer.model.*;
import com.liu.wanxinp2p.api.depository.model.GatewayRequest;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.common.util.HttpClientUitl;
import com.liu.wanxinp2p.consumer.common.SecurityUtil;
import com.liu.wanxinp2p.consumer.service.ConsumerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;


@RestController
@Api(tags = "ConsumerController",description = "用户服务的Controller")
@Slf4j
public class ConsumerController implements ConsumerAPI {

    @Autowired
    private ConsumerService consumerService;

    @Value("${depository.url}")
    private String depositoryURL;

    @ApiOperation(value = "用户注册",httpMethod = "POST",notes = "根据前端用户提交的信息进行注册")
    @ApiImplicitParam(name = "consumerRegisterDTO",value = "用户注册信息",required = true,dataType = "ConsumerRegisterDTO",paramType = "body")
    @Override
    @PostMapping("/consumers")
    public RestResponse register(@RequestBody ConsumerRegisterDTO consumerRegisterDTO) {
        System.out.println(consumerService);
        consumerService.register(consumerRegisterDTO);
        return RestResponse.success();
    }

    /**
     * 生成开户请求数据
     * @param consumerRequest
     * @return
     */
    @ApiOperation(value = "生成开户请求数据",httpMethod = "POST", notes = "根据前端用户提交的信息生成开户请求数据")
    @ApiImplicitParam(name = "consumerRequest", value = "开户信息", required = true,
            dataType = "ConsumerRequest", paramType = "body")
    @PostMapping("/my/consumers")
    @Override
    public RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest consumerRequest) {

        System.out.println(consumerRequest);
        //获得当前正在登录的用户的信息
        consumerRequest.setMobile(SecurityUtil.getUser().getMobile());
        return consumerService.createConsumer(consumerRequest);
    }

    @Override
    @ApiOperation("获取登录用户信息")
    @GetMapping("/my/consumers")
    public RestResponse<ConsumerDTO> getMyConsumer() {
        LoginUser loginUser = SecurityUtil.getUser();
        ConsumerDTO consumerDTO = consumerService.getByMobile(loginUser.getMobile(),true);
        return RestResponse.success(consumerDTO);
    }

    @ApiOperation("获取借款人信息")
    @ApiImplicitParam(name = "id", value = "用户标识", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/my/borrowers/{id}")
    @Override
    public RestResponse<BorrowDTO> getBorrower(@PathVariable Long id) {
        return RestResponse.success(consumerService.getBorrower(id));
    }

    @Override
    @ApiOperation("获取用户可用余额")
    @ApiImplicitParam(name="userNo", value = "用户编码", required = true,dataType = "String", paramType = "path")
    @GetMapping("/l/balances/{userNo}")
    public RestResponse<BalanceDetailsDTO> getBalance(@PathVariable String userNo) {
        RestResponse<BalanceDetailsDTO> balanceFromDepository = getBalanceFromDepository(userNo);
        return balanceFromDepository;
    }

    @Override
    @ApiOperation("获取当前登录用户余额")
    @GetMapping("/my/balances")
    public RestResponse<BalanceDetailsDTO> getMyBalance() {
        ConsumerDTO consumerDTO = consumerService.getByMobile(SecurityUtil.getUser().getMobile(), false);
        return getBalanceFromDepository(consumerDTO.getUserNo());
    }

    @Override
    @ApiOperation("生成充值请求数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "amount", value = "金额", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "callbackURL", value = "通知结果回调Url", required = true, dataType = "String", paramType = "query")
    })
    @GetMapping("/my/recharge-records")
    public RestResponse<GatewayRequest> createRechargeRecord(@RequestParam String amount,@RequestParam String callbackUrl) {
        RechargeRequest rechargeRequest = new RechargeRequest();
        rechargeRequest.setAmount(new BigDecimal(amount));
        rechargeRequest.setCallbackUrl(callbackUrl);
        System.out.println(rechargeRequest);
        return null;
    }

    @Override
    @ApiOperation("获取借款人用户信息-供微服务访问")
    @ApiImplicitParam(name = "id", value = "用户标识", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/l/borrowers/{id}")
    public RestResponse<BorrowDTO> getBorrowerMobile(@PathVariable("id") Long id) {
        return RestResponse.success(consumerService.getBorrower(id));
    }


    @ApiOperation(value = "获取当前登录用户",httpMethod = "GET", notes = "获得存储在token中的当前登录用户信息")
    @ApiImplicitParam(name = "mobile", value = "用户手机号码",required = true,dataType = "String", paramType = "path")
    @GetMapping("/l/currentConsumer/{mobile}")
    @Override
    public RestResponse<ConsumerDTO> getCurrentConsumer(@PathVariable String mobile) {
        ConsumerDTO consumerDTO = consumerService.getByMobile(mobile,true);
        return RestResponse.success(consumerDTO);
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/m/consumers/test")
    public RestResponse<LoginUser> testZuulFilter(HttpServletRequest request) {
        //token信息解密
        LoginUser currentUser = (LoginUser) request.getAttribute("current_user");
        return RestResponse.success(currentUser);
    }

    /**
     * 远程调用存管系统获取用户余额信息
     * @param userNo 用户编码
     * @return
     */
    private RestResponse<BalanceDetailsDTO> getBalanceFromDepository(String userNo) {
            String url = depositoryURL + "/balance-details/" + userNo;
            BalanceDetailsDTO balanceDetailsDTO = null;
            String response = null;
            try {
                response = HttpClientUitl.doGet(url, null);
                System.out.println("response:" + response);
                balanceDetailsDTO = JSON.parseObject(response,BalanceDetailsDTO.class);
                return RestResponse.success(balanceDetailsDTO);
            } catch (Exception e) {
                log.warn("调用存管系统{}获取余额失败",url,e);
            }
            return RestResponse.validfail("获取余额失败");
    }
}
