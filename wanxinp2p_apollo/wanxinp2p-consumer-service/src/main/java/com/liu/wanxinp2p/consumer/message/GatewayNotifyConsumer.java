package com.liu.wanxinp2p.consumer.message;

import com.alibaba.fastjson.JSON;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import com.liu.wanxinp2p.consumer.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Consumer消费RocketMQ中的消息
 */
//@Component
//@Slf4j
//public class GatewayNotifyConsumer {
//
//    @Autowired
//    private ConsumerService consumerService;
//
//    public GatewayNotifyConsumer(
//            @Value("${rocketmq.consumer.group}")String consumerGroup,
//            @Value("${rocketmq.name-server}")String mqNameServer) throws MQClientException {
//        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
//        //设置NameServer地址
//        defaultMQPushConsumer.setNamesrvAddr(mqNameServer);
//        //从最后的偏移量开始消费
//        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
//        //订阅 TOPIC=TP_GATEWAY_NOTIFY_AGENT
//        defaultMQPushConsumer.subscribe("TP_GATEWAY_NOTIFY_AGENT","*");
//        //注册监听器
//        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently(){
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//                try {
//                    Message message = msgs.get(0);
//                    String topic = message.getTopic();
//                    String tags = message.getTags();
//                    String body = new String(message.getBody(), StandardCharsets.UTF_8);
//                    if (tags.equals("PERSONAL_REGISTER")) {
//                        DepositoryConsumerResponse response = JSON.parseObject(body,DepositoryConsumerResponse.class);
//                        System.out.println("收到消息了:"+response);
//                        consumerService.modifyResult(response);
//                    }
//                } catch (Exception e) {
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//        defaultMQPushConsumer.start();
//    }
//}
