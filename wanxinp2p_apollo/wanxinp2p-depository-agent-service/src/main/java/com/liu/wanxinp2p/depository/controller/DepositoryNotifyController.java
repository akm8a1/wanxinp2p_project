package com.liu.wanxinp2p.depository.controller;

import com.alibaba.fastjson.JSON;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import com.liu.wanxinp2p.common.util.EncryptUtil;
import com.liu.wanxinp2p.depository.message.GatewayMessageProducer;
import com.liu.wanxinp2p.depository.service.DeDuplicationService;
import com.liu.wanxinp2p.depository.service.DepositoryRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 银行存管系统的开户结果通知
 * 银行存管系统处理了开户业务之后，会调用这里的这个服务，从而将处理的数据交给银行代理服务
 */
@Api(tags = "DEPOSITORY-AGENT",description = "银行存管系统通知服务")
@RestController
public class DepositoryNotifyController {

    @Autowired
    private DepositoryRecordService depositoryRecordService;

    @Autowired
    private GatewayMessageProducer gatewayMessageProducer;

    @Autowired
    private DeDuplicationService deDuplicationService;

    /**
     * 这个接口会开放给银行存管系统，并被银行方调用，从而修改存管代理服务的交易记录并发送异步消息给consumer-service从而更新数据
     * @param serviceName
     * @param platformNo
     * @param signature
     * @param reqData
     * @return
     */
    @ApiOperation("接收银行存管系统的开户回调结果")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "serviceName", value = "请求的存管接口名",required = true, dataType = "String", paramType =
                    "query"),
            @ApiImplicitParam(name = "platformNo", value = "平台编号，平台与存管系统签约时获取", required = true, dataType = "String", paramType =
                    "query"),
            @ApiImplicitParam(name = "signature", value = "对reqData参数的签名", required = true, dataType = "String", paramType =
                    "query"),
            @ApiImplicitParam(name = "reqData", value = "业务数据报文，json格式", required = true, dataType = "String", paramType =
                    "query")
    })
    //serviceName必须是PERSONAL_REGISTER,即个人开户
    @RequestMapping(value = "/gateway", method = RequestMethod.GET, params = "serviceName=PERSONAL_REGISTER")
    public String receiveDepositoryCreateConsumerResult(
            @RequestParam("serviceName") String serviceName,
            @RequestParam("platformNo") String platformNo,
            @RequestParam("signature") String signature,
            @RequestParam("reqData") String reqData) {
        //1.更新数据
        //解码之后得到封装的信息
        DepositoryConsumerResponse response = JSON.parseObject(EncryptUtil.decodeUTF8StringBase64(reqData),
                DepositoryConsumerResponse.class);
        System.out.println("发送消息:" + response);
        System.out.println("消息的requestNo:" + response.getRequestNo());
        //2.发送事务性消息
        depositoryRecordService.sendCreateConsumerMessage(response);
        return "OK";
    }
}
