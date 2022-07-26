package com.liu.wanxinp2p.transaction.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.transaction.common.constant.ProjectCode;
import com.liu.wanxinp2p.transaction.entity.Project;
import com.liu.wanxinp2p.transaction.mapper.ProjectMapper;
import com.liu.wanxinp2p.transaction.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * RocketMQ事务消息监听器
 */
@Component
@Slf4j
@RocketMQTransactionListener(txProducerGroup = "PID_START_REPAYMENT")
public class P2PTransactionListenerImpl implements RocketMQLocalTransactionListener {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 执行本地事务
     * @param msg 本地刚刚发送的消息
     * @param arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("满标放款执行本地事务:将项目状态更新为REPAYING");
        //1.解析消息
        JSONObject jsonObject = JSON.parseObject(new String((byte[]) msg.getPayload()));
        Project project = JSONObject.parseObject(jsonObject.getString("project"),Project.class);
        //2.执行本地事务
        Boolean result = projectService.updateProjectStatusAndStartRepayment(project);
        //3.返回执行结果
        if (result) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 执行事务回查
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        log.info("满标放款事务回查");
        //1.解析消息
        JSONObject jsonObject = JSON.parseObject(new String((byte[])msg.getPayload()));
        Project project = JSONObject.parseObject(jsonObject.getString("project"),Project.class);
        //2.查询标的状态
        Project pro = projectMapper.selectById(project.getId());
        //3.返回结果
        if (pro.getProjectStatus().equals(ProjectCode.REPAYING.getCode())) {
            //执行本地事务之后状态就会改变
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
