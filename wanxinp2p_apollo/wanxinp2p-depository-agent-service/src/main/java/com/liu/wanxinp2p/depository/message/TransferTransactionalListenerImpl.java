package com.liu.wanxinp2p.depository.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import com.liu.wanxinp2p.depository.entity.DeDuplication;
import com.liu.wanxinp2p.depository.service.DeDuplicationService;
import com.liu.wanxinp2p.depository.service.DepositoryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * RocketMQ事物消息监听器
 */
@Component
@Slf4j
//发送者组为p2p_depository_agent的事务监听器
@RocketMQTransactionListener(txProducerGroup = "p2p_depository_agent")
public class TransferTransactionalListenerImpl implements RocketMQLocalTransactionListener {

    @Autowired
    private DepositoryRecordService depositoryRecordService;

    @Autowired
    private DeDuplicationService deDuplicationService;

    /**
     * 执行本地的事物，在事物消息中会被回调
     * @param msg 之前发出的消息
     * @param arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        //1.接收并解析消息
        JSONObject jsonObject = JSON.parseObject(new String((byte[])msg.getPayload()));
        DepositoryConsumerResponse response = JSON.parseObject(jsonObject.getString("message"),DepositoryConsumerResponse.class);
        //2.执行本地事务
        Boolean isCommit = true;
        try {
            depositoryRecordService.modifyRequestStatus(response.getRequestNo(),response.getStatus());
        } catch (Exception e) {
            isCommit = false;
        }
        //3.返回本地事务执行结果
        if (isCommit) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 事物回查
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        //1.接收并解析消息
        JSONObject jsonObject = JSON.parseObject(new String((byte[])msg.getPayload()));
        DepositoryConsumerResponse response = JSON.parseObject(jsonObject.getString("message"),DepositoryConsumerResponse.class);
        //2.查询de_duplicaiton表
        int count = deDuplicationService.count(
                new QueryWrapper<DeDuplication>().eq("tx_no",response.getRequestNo()));
        //3.根据查询结果返回本地事务执行的状态
        if (count > 0) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
