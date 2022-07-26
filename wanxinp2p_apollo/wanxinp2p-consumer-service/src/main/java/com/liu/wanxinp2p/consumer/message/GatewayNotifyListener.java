package com.liu.wanxinp2p.consumer.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import com.liu.wanxinp2p.consumer.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 事务性消息的接收器 - 监听用户开户消息
 * RocketMQMessageListener即监听器注解 selectorExpression即tags
 */
@Component
@RocketMQMessageListener(consumerGroup = "CID_P2P_GATEWAY_NOTIFY",
                        topic = "TP_GATEWAY_NOTIFY_AGENT",selectorExpression = "PERSONAL_REGISTER")
@Slf4j
public class GatewayNotifyListener implements RocketMQListener<String> {

    @Autowired
    private ConsumerService consumerService;

    //接收到消息之后的逻辑
    @Override
    public void onMessage(String message) {
        log.info("开始消费——开户信息(来自银行存管代理服务)");
        JSONObject jsonObject = JSON.parseObject(message);
        DepositoryConsumerResponse response = JSON.parseObject(jsonObject.getString("message"),DepositoryConsumerResponse.class);
        consumerService.modifyResult(response);
    }
}
