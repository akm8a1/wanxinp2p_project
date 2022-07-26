package com.liu.wanxinp2p.depository.controller;

import com.liu.wanxinp2p.api.consumer.model.ConsumerRequest;
import com.liu.wanxinp2p.api.consumer.model.RechargeRequest;
import com.liu.wanxinp2p.api.depository.DepositoryAgentApi;
import com.liu.wanxinp2p.api.depository.model.*;
import com.liu.wanxinp2p.api.transaction.model.ModifyProjectStatusDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.depository.service.DepositoryRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@Api(tags = "depository-agent",description = "存管代理服务")
@RestController
public class DepositoryAgentController implements DepositoryAgentApi {

    @Autowired
    private DepositoryRecordService depositoryRecordService;

    /**
     * 操作的是代理服务的表，并给consumer-service返回结果
     * @param consumerRequest 开户信息
     * @return
     */
    @ApiOperation("生成开户请求数据")
    @ApiImplicitParam(name = "consumerRequest",value = "开户信息",required = true,
    dataType = "ConsumerRequest", paramType = "body")
    @PostMapping("/l/consumers")
    @Override
    public RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest consumerRequest) {
        return RestResponse.success(depositoryRecordService.createConsumer(consumerRequest));
    }

    @Override
    @ApiOperation(value = "向存管系统发送标的信息")
    @ApiImplicitParam(name = "projectDTO", value = "标的信息", required = true, dataType = "ProjectDTO", paramType = "body")
    @PostMapping("/l/createProject")
    public RestResponse<String> createProject(@RequestBody ProjectDTO projectDTO) throws UnsupportedEncodingException {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = depositoryRecordService.createProject(projectDTO);
        System.out.println("depositoryResponse:" + depositoryResponse);
        RestResponse<String> restResponse = new RestResponse<String>();
        restResponse.setResult(depositoryResponse.getRespData().getRespCode());
        restResponse.setMsg(depositoryResponse.getRespData().getRespMsg());
        System.out.println("restResponse:"+restResponse);
        return restResponse;
    }


    @Override
    @ApiOperation("预授权处理")
    @ApiImplicitParam(name = "userAutoPreTransactionRequest", value = "平台向存管系统发送标的信息", required = true, dataType = "userAutoPreTransactionRequest", paramType = "body")
    @PostMapping("/l/user-auto-pre-transaction")
    public RestResponse<String> userAutoPreTransaction(@RequestBody UserAutoPreTransactionRequest userAutoPreTransactionRequest) throws UnsupportedEncodingException {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = depositoryRecordService.userAutoPreTransaction(userAutoPreTransactionRequest);
        return getRestResponse(depositoryResponse);
    }

    @Override
    @ApiOperation("审核标的满标放款")
    @ApiImplicitParam(name="loanRequest",value = "标的满标放款信息", required = true,dataType = "LoanRequest",paramType = "body")
    @PostMapping("/l/confirm-loan")
    public RestResponse<String> confirmLoan(@RequestBody LoanRequest loanRequest) throws UnsupportedEncodingException {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = depositoryRecordService.confirmLoan(loanRequest);
        return getRestResponse(depositoryResponse);
    }

    @Override
    @ApiOperation("修改标的状态")
    @ApiImplicitParam(name="modifyProjectStatusDTO",value = "修改标的状态DTO",required = true,dataType = "ModifyProjectStatusDTO",paramType = "body")
    @PostMapping("/l/modify-project-status")
    public RestResponse<String> modifyProjectStatus(@RequestBody ModifyProjectStatusDTO modifyProjectStatusDTO) throws UnsupportedEncodingException {
        System.out.println("接收参数:"+modifyProjectStatusDTO);
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse =
                depositoryRecordService.modifyProjectStatus(modifyProjectStatusDTO);
        return getRestResponse(depositoryResponse);
    }

    @Override
    @ApiOperation("生成充值请求数据")
    @ApiImplicitParam(name = "rechargeRequest", value = "重置信息", required = true, dataType = "RechargeRequest", paramType = "body")
    @PostMapping("/l/recharges")
    public RestResponse<GatewayRequest> createRechargeRecord(@RequestBody RechargeRequest rechargeRequest) {
//        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = depositoryRecordService.confirmRepayment()
        return null;
    }

    @Override
    @ApiOperation("确认还款")
    @ApiImplicitParam(name = "repaymentRequest", value = "还款信息", required = true, dataType = "RepaymentRequest", paramType = "body")
    @PostMapping("/l/confirm-repayment")
    public RestResponse<String> confirmRepayment(@RequestBody RepaymentRequest repaymentRequest) {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = depositoryRecordService.confirmRepayment(repaymentRequest);
        return getRestResponse(depositoryResponse);
    }

    private RestResponse<String> getRestResponse(DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse) {
        RestResponse<String> restResponse = new RestResponse<>();
        restResponse.setResult(depositoryResponse.getRespData().getRespCode());
        restResponse.setMsg(depositoryResponse.getRespData().getRespMsg());
        return restResponse;
    }
}
