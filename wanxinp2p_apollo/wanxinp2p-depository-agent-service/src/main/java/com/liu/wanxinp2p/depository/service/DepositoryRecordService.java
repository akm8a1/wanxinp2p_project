package com.liu.wanxinp2p.depository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.wanxinp2p.api.consumer.model.ConsumerRequest;
import com.liu.wanxinp2p.api.depository.model.*;
import com.liu.wanxinp2p.api.transaction.model.ModifyProjectStatusDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.depository.entity.DepositoryRecord;

import java.io.UnsupportedEncodingException;

/**
 * 存管对接服务
 */
public interface DepositoryRecordService extends IService<DepositoryRecord> {

    /**
     * 开通存管账号
     * @param consumerRequest
     * @return
     */
    GatewayRequest createConsumer(ConsumerRequest consumerRequest);

    /**
     * 根据请求流水号更新请求状态——本地事务
     * @param requestNo 请求号
     * @param status 响应状态
     * @return
     */
    Boolean modifyRequestStatus(String requestNo,Integer status);

    /**
     * 发送开通存管账号之后的Consumer-Service更新消息-发送消息
     * @param response 银行存管系统那边发送的信息
     */
    void sendCreateConsumerMessage(DepositoryConsumerResponse response);

    /**
     * 保存标的
     * @param projectDTO 标的数据
     * @return
     */
    DepositoryResponseDTO<DepositoryBaseResponse> createProject(ProjectDTO projectDTO) throws UnsupportedEncodingException;

    /**
     * 投标预处理
     * @param userAutoPreTransactionRequest
     * @return
     */
    DepositoryResponseDTO<DepositoryBaseResponse> userAutoPreTransaction(UserAutoPreTransactionRequest userAutoPreTransactionRequest) throws UnsupportedEncodingException;

    /**
     * 审核满标放款
     * @param loanRequest
     * @return
     */
    DepositoryResponseDTO<DepositoryBaseResponse> confirmLoan(LoanRequest loanRequest) throws UnsupportedEncodingException;

    /**
     * 修改标的状态
     * @param modifyProjectStatusDTO
     * @return
     */
    DepositoryResponseDTO<DepositoryBaseResponse> modifyProjectStatus(ModifyProjectStatusDTO modifyProjectStatusDTO) throws UnsupportedEncodingException;

    /**
     *
     * @param repaymentRequest
     * @return
     */
    DepositoryResponseDTO<DepositoryBaseResponse> confirmRepayment(RepaymentRequest repaymentRequest);
}
