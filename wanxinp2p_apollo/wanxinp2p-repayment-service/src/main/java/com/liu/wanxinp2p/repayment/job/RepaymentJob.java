package com.liu.wanxinp2p.repayment.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.liu.wanxinp2p.repayment.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepaymentJob implements SimpleJob {

    @Autowired
    private RepaymentService repaymentService;

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("执行定时任务");
//        //调用业务层执行任务
//        repaymentService.executeRepayment(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
//        //发送短信提醒消息
//        /**
//         * 查询明天会过期的并发送短信
//         */
//        repaymentService.sendRepaymentNotify(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}
