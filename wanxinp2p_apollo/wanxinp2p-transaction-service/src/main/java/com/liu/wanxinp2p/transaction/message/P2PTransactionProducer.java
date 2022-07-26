package com.liu.wanxinp2p.transaction.message;

import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import com.liu.wanxinp2p.transaction.entity.Project;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class P2PTransactionProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void updateProjectStatusAndStartRepayment(Project project, ProjectWithTendersDTO projectWithTendersDTO) {
        //1. 构造消息
        JSONObject object = new JSONObject();
        object.put("project",project);
        object.put("projectWithTendersDTO",projectWithTendersDTO);
        Message<String> msg = MessageBuilder.withPayload(object.toJSONString()).build();
        //2. 发送消息
        rocketMQTemplate.sendMessageInTransaction("PID_START_REPAYMENT","TP_START_REPAYMENT",msg,null);
    }
}
