package com.liu.wanxinp2p.repayment.message;

import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.api.depository.model.RepaymentRequest;
import com.liu.wanxinp2p.repayment.entity.RepaymentPlan;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class RepaymentProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        //1.构造消息
        JSONObject object = new JSONObject();
        object.put("repaymentPlan",repaymentPlan);
        object.put("repaymentRequest",repaymentRequest);
        Message<String> msg = MessageBuilder.withPayload(object.toJSONString()).build();
        //2.发送消息
        rocketMQTemplate.sendMessageInTransaction("PID_CONFIRM_REPAYMENT",
                "TP_CONFIRM_REPAYMENT", msg, null);
    }
}
