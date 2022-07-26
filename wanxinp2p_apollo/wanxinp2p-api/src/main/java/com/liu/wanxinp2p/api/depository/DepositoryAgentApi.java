package com.liu.wanxinp2p.api.depository;

import com.liu.wanxinp2p.api.consumer.model.ConsumerRequest;
import com.liu.wanxinp2p.api.consumer.model.RechargeRequest;
import com.liu.wanxinp2p.api.depository.model.*;
import com.liu.wanxinp2p.api.transaction.model.ModifyProjectStatusDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;

/**
 * 银行存管系统代理服务API
 */
public interface DepositoryAgentApi {

    /**
     * 开通存管账户
     * @param consumerRequest 开户信息
     * @return
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);

    /**
     * 向银行存管系统发送标的信息
     * @param projectDTO 标的
     * @return
     */
    RestResponse<String> createProject(ProjectDTO projectDTO) throws UnsupportedEncodingException;

    /**
     * 预授权处理
     * @param userAutoPreTransactionRequest 预授权处理信息
     * @return
     */
    RestResponse<String> userAutoPreTransaction(UserAutoPreTransactionRequest userAutoPreTransactionRequest) throws UnsupportedEncodingException;

    /**
     * 审核标的满标放款
     * @param loanRequest
     * @return
     */
    RestResponse<String> confirmLoan(LoanRequest loanRequest) throws UnsupportedEncodingException;

    /**
     * 修改标的状态
     * @param modifyProjectStatusDTO
     * @return
     */
    RestResponse<String> modifyProjectStatus(ModifyProjectStatusDTO modifyProjectStatusDTO) throws UnsupportedEncodingException;

    /**
     * 用户充值
     * @param rechargeRequest
     * @return
     */
    RestResponse<GatewayRequest> createRechargeRecord(@RequestBody RechargeRequest rechargeRequest);

    /**
     * 还款确认
     * @param repaymentRequest 还款信息
     * @return
     */
    RestResponse<String> confirmRepayment(RepaymentRequest repaymentRequest);

}
