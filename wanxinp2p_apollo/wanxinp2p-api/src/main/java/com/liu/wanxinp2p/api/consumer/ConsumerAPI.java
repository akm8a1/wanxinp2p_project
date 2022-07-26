package com.liu.wanxinp2p.api.consumer;

import com.liu.wanxinp2p.api.consumer.model.*;
import com.liu.wanxinp2p.api.depository.model.GatewayRequest;
import com.liu.wanxinp2p.common.domain.RestResponse;

public interface ConsumerAPI {

    /**
     * 用户注册
     * @param consumerRegisterDTO 用回顾注册信息
     * @return
     */
    RestResponse register(ConsumerRegisterDTO consumerRegisterDTO);

    /**
     * 生成开户请求数据
     * @param consumerRequest
     * @return
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);


    /**
     * 获得当前登录用户
     * @return
     */
    RestResponse<ConsumerDTO> getCurrentConsumer(String mobile);

    /**
     * 获取当前登录用户
     * @return
     */
    RestResponse<ConsumerDTO> getMyConsumer();

    /**
     * 获取借款人用户信息
     * @param id
     * @return
     */
    RestResponse<BorrowDTO> getBorrower(Long id);

    /**
     * 根据userNo获取用户余额信息
     * @param userNo
     * @return
     */
    RestResponse<BalanceDetailsDTO> getBalance(String userNo);

    /**
     * 获取当前登录用户余额信息
     * @return
     */
    RestResponse<BalanceDetailsDTO> getMyBalance();

    /**
     * 生成充值请求数据
     * @param amount 充值金额
     * @param callbackURL 回调地址
     * @return
     */
    RestResponse<GatewayRequest> createRechargeRecord(String amount,String callbackURL);

    /**
     * 获取借款人用户信息-供微服务访问
     * @param id
     * @return
     */
    RestResponse<BorrowDTO> getBorrowerMobile(Long id);
}
