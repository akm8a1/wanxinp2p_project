package com.liu.wanxinp2p.consumer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.wanxinp2p.api.consumer.model.*;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import com.liu.wanxinp2p.api.depository.model.GatewayRequest;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.consumer.entity.Consumer;

public interface ConsumerService extends IService<Consumer> {

    /**
     * 检测用户是否存在
     * @param mobile 手机号码
     * @return
     */
    Integer checkMobile(String mobile) ;

    /**
     * 用户注册
     * @param consumerRegisterDTO
     */
    void register(ConsumerRegisterDTO consumerRegisterDTO);

    /**
     * 生成开户数据
     * @param consumerRequest
     * @return
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);

    /**
     * 更新开户结果
     * @param response 银行存管代理服务发送的DepositoryConsumerResponse消息，里面包含了银行卡的信息、从而更新consumer库中
     *                 bank_card表的银行卡信息
     * @return
     */
    Boolean modifyResult(DepositoryConsumerResponse response);

    /**
     * 私有方法 : 根据电话号码获取用户
     * @param mobile 电话号码
     * @param throwEx MyBatisPlus 参数， 如果为true就直接执行，如果为false就查询列表并返回第一个
     * @return
     */
    ConsumerDTO getByMobile(String mobile, Boolean throwEx);

    /**
     * 获取借款人基本信息
     * @param id
     * @return
     */
    BorrowDTO getBorrower(Long id);

    /**
     * 用户充值
     * @param rechargeRequest
     * @return
     */
    RestResponse<GatewayRequest> createRechargeRecord(RechargeRequest rechargeRequest);
}
