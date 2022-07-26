package com.liu.wanxinp2p.depository.message;

import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 消息生产者 : 发送银行存管系统的处理响应给消费者服务
 */
@Component
public class GatewayMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 普通消息机制
     * @param response
     */
    public void personalRegister(DepositoryConsumerResponse response) {
        //destination:topic:tags
        /**
         * topic:TP_GATEWAY_NOTIFY_AGENT
         * tags:PERSONAL_REGISTER
         */
        rocketMQTemplate.convertAndSend("TP_GATEWAY_NOTIFY_AGENT:PERSONAL_REGISTER",response);
    }
    //实现事物消息
    public void sendPersonalRegister(DepositoryConsumerResponse response) {
        //1.构造消息
        JSONObject object = new JSONObject();
        object.put("message",response);
        Message<String> message = MessageBuilder.withPayload(object.toJSONString()).build();
        //2.发送消息
        rocketMQTemplate.sendMessageInTransaction("p2p_depository_agent","TP_GATEWAY_NOTIFY_AGENT:PERSONAL_REGISTER",message,null);
    }
}
