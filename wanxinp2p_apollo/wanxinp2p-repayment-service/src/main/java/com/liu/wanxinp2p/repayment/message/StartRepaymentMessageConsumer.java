package com.liu.wanxinp2p.repayment.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import com.liu.wanxinp2p.repayment.service.RepaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "TP_START_REPAYMENT",consumerGroup = "CID_START_REPAYMENT")
public class StartRepaymentMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private RepaymentService repaymentService;

    @Override
    public void onMessage(String message) {
        log.info("Transaction-Service消费消息:{}",message);
        //1.解析消息
        JSONObject jsonObject = JSON.parseObject(message);
        ProjectWithTendersDTO projectWithTendersDTO = JSONObject.parseObject(jsonObject.getString("projectWithTendersDTO"), ProjectWithTendersDTO.class);
        //2.调用业务层，执行本地事务:开始还款(生成还款计划与收款计划】)
        repaymentService.startRepayment(projectWithTendersDTO);
    }
}
