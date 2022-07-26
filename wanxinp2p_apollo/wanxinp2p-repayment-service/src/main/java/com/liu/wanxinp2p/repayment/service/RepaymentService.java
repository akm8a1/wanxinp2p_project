package com.liu.wanxinp2p.repayment.service;

import com.liu.wanxinp2p.api.depository.model.RepaymentRequest;
import com.liu.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import com.liu.wanxinp2p.repayment.entity.RepaymentDetail;
import com.liu.wanxinp2p.repayment.entity.RepaymentPlan;

import java.util.List;

public interface RepaymentService {

    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */
    String startRepayment(ProjectWithTendersDTO projectWithTendersDTO);

    /**
     * 查询到期还款计划
     * @param date 格式为yyyy-MM-dd
     * @return
     */
    List<RepaymentPlan> selectDueRepayment(String date,int shardingCount,int shardingItem);

    public List<RepaymentPlan> selectDueRepayment(String date);

    /**
     * 根据还款计划生成还款明细并保存
     * @param repaymentPlan
     * @return
     */
    RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan);

    /**
     * 执行还款
     * @param date
     */
    void executeRepayment(String date);

    /**
     * 还款预处理：冻结借款人应还金额
     * @param repaymentPlan 还款计划
     * @param preRequestNo 预还款请求号
     * @return
     */
    Boolean preRepayment(RepaymentPlan repaymentPlan,String preRequestNo);

    /**
     * 确认还款处理
     * @param repaymentPlan
     * @param repaymentRequest
     * @return
     */
    Boolean confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest);

    /**
     * 远程调用确认还款接口
     * @param repaymentPlan
     * @param repaymentRequest
     */
    void invokeConfirmRepayment(RepaymentPlan repaymentPlan,RepaymentRequest repaymentRequest);

    /**
     * 发送还款短信通知
     * @param date
     */
    void sendRepaymentNotify(String date);
}
