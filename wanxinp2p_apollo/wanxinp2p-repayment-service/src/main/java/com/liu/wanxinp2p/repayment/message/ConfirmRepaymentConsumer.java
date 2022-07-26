package com.liu.wanxinp2p.repayment.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.api.depository.model.RepaymentRequest;
import com.liu.wanxinp2p.repayment.entity.RepaymentPlan;
import com.liu.wanxinp2p.repayment.service.RepaymentService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "TP_CONFIRM_REPAYMENT", consumerGroup = "CID_CONFIRM_REPAYMENT")
public class ConfirmRepaymentConsumer implements RocketMQListener<String> {

    @Autowired
    private RepaymentService repaymentService;

    @Override
    public void onMessage(String message) {
        //1.解析消息
        JSONObject jsonObject = JSON.parseObject(message);
        RepaymentPlan repaymentPlan = JSONObject.parseObject(jsonObject.getString("repaymentPlan"),RepaymentPlan.class);
        RepaymentRequest repaymentRequest = JSONObject.parseObject(jsonObject.getString("repaymentRequest"), RepaymentRequest.class);
        //2.执行业务
        repaymentService.invokeConfirmRepayment(repaymentPlan,repaymentRequest);
    }
}
