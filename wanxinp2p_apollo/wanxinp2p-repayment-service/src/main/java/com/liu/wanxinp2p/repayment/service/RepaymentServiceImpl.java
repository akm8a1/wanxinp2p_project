package com.liu.wanxinp2p.repayment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.liu.wanxinp2p.api.consumer.model.BorrowDTO;
import com.liu.wanxinp2p.api.depository.model.DepositoryReturnCode;
import com.liu.wanxinp2p.api.depository.model.RepaymentDetailRequest;
import com.liu.wanxinp2p.api.depository.model.RepaymentRequest;
import com.liu.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import com.liu.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.api.transaction.model.TenderDTO;
import com.liu.wanxinp2p.common.domain.*;
import com.liu.wanxinp2p.common.util.CodeNoUtil;
import com.liu.wanxinp2p.common.util.DateUtil;
import com.liu.wanxinp2p.repayment.agent.ConsumerApiAgent;
import com.liu.wanxinp2p.repayment.agent.DepositoryAgentApiAgent;
import com.liu.wanxinp2p.repayment.entity.ReceivableDetail;
import com.liu.wanxinp2p.repayment.entity.ReceivablePlan;
import com.liu.wanxinp2p.repayment.entity.RepaymentDetail;
import com.liu.wanxinp2p.repayment.entity.RepaymentPlan;
import com.liu.wanxinp2p.repayment.mapper.PlanMapper;
import com.liu.wanxinp2p.repayment.mapper.ReceivableDetailMapper;
import com.liu.wanxinp2p.repayment.mapper.ReceivablePlanMapper;
import com.liu.wanxinp2p.repayment.mapper.RepaymentDetailMapper;
import com.liu.wanxinp2p.repayment.message.RepaymentProducer;
import com.liu.wanxinp2p.repayment.model.EqualInterestRepayment;
import com.liu.wanxinp2p.repayment.sms.SmsService;
import com.liu.wanxinp2p.repayment.util.RepaymentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RepaymentServiceImpl implements RepaymentService{

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private ReceivablePlanMapper receivablePlanMapper;

    @Autowired
    private ReceivableDetailMapper receivableDetailMapper;

    @Autowired
    private RepaymentDetailMapper repaymentDetailMapper;

    @Autowired
    private DepositoryAgentApiAgent depositoryAgentApiAgent;

    @Autowired
    private RepaymentProducer repaymentProducer;

    @Autowired
    private ConsumerApiAgent consumerApiAgent;

    @Autowired
    private SmsService smsService;

    @Override
    public String startRepayment(ProjectWithTendersDTO projectWithTendersDTO) {
        //1.生成借款人还款计划
        //1.1 获取标的信息
        ProjectDTO projectDTO = projectWithTendersDTO.getProject();
        //1.2 获取投标信息
        List<TenderDTO> tenders = projectWithTendersDTO.getTenders();
        //1.3 计算还款的月数
        Double ceil = Math.ceil(projectDTO.getPeriod()/30.0);
        Integer month = ceil.intValue();
        //1.4 还款方式，只针对等额本息
        String repaymentWay = projectDTO.getRepaymentWay();
        if (repaymentWay.equals(RepaymentWayCode.FIXED_REPAYMENT.getCode())) {
            //1.5 生成还款计划
            //还款计划中存在着总还款金额、还款本金、还款利息以及每个月的还款额、还款利息、还款本金、平台抽息等
            EqualInterestRepayment fixedRepayment = RepaymentUtil.fixedRepayment(
                    projectDTO.getAmount(),projectDTO.getBorrowerAnnualRate(),
                    month,projectDTO.getCommissionAnnualRate());
            //1.6 保存还款计划
            List<RepaymentPlan> planList = saveRepaymentPlan(projectDTO,fixedRepayment);
            //2.生成投资人应收明细
            //2.1 根据投标信息生成应收明细
            for (TenderDTO tender : tenders) {
                System.out.println(tender.getAmount() + ":" + tender.getProjectAnnualRate() + ":" + month + ":" + projectWithTendersDTO.getCommissionInvestorAnnualRate());
                //当前投资人的收款明细
                EqualInterestRepayment receipt = RepaymentUtil.fixedRepayment(
                        tender.getAmount(),tender.getProjectAnnualRate(),
                        month,projectWithTendersDTO.getCommissionInvestorAnnualRate()
                );
                /*
                投标人的收款明细需要还款信息，遍历还款计划，把还款期数与投资人应收期数对应上
                 */
                for (RepaymentPlan repaymentPlan : planList) {
                    //2.2 保存应收明细到数据库
                    saveReceivablePlan(repaymentPlan,tender,receipt);
                }
            }
        } else {
            return "-1";
        }
        return DepositoryReturnCode.RETURN_CODE_00000.getCode();
    }

    @Override
    public List<RepaymentPlan> selectDueRepayment(String date, int shardingCount, int shardingItem) {
        return planMapper.selectDueRepayment(date,shardingCount,shardingItem);
    }

    @Override
    public List<RepaymentPlan> selectDueRepayment(String date) {
        return planMapper.selectDueRepayment(date);
    }

    @Override
    public RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan) {
        QueryWrapper<RepaymentDetail> queryWrapper = new QueryWrapper<RepaymentDetail>().eq("REPAYMENT_PLAN_ID",repaymentPlan.getId());
        RepaymentDetail repaymentDetail = repaymentDetailMapper.selectOne(queryWrapper);
        if (repaymentDetail == null) {
            repaymentDetail = new RepaymentDetail();
            //还款计划标识
            repaymentDetail.setRepaymentPlanId(repaymentPlan.getId());
            //实还本息
            repaymentDetail.setAmount(repaymentPlan.getAmount());
            //实际还款时间
            repaymentDetail.setRepaymentDate(LocalDateTime.now());
            //请求流水号
            repaymentDetail.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
            //未同步
            repaymentDetail.setStatus(StatusCode.STATUS_OUT.getCode());
            //保存数据
            System.out.println(repaymentDetail);
            repaymentDetailMapper.insert(repaymentDetail);
        }
        return repaymentDetail;
    }

    @Override
    public void executeRepayment(String date) {
        //查询所有到期的还款计划
        List<RepaymentPlan> repaymentPlanList = selectDueRepayment(date);
        for (RepaymentPlan repaymentPlan : repaymentPlanList) {
            //还款明细
            RepaymentDetail repaymentDetail = saveRepaymentDetail(repaymentPlan);
            //还款预处理
            String preRequestNo = repaymentDetail.getRequestNo();
            Boolean preRepaymentResult = preRepayment(repaymentPlan,preRequestNo);
            if (preRepaymentResult) {
                //构造还款信息请求数据
                RepaymentRequest repaymentRequest = generateRepaymentRequest(repaymentPlan,preRequestNo);
                //发送确认还款事务消息
                repaymentProducer.confirmRepayment(repaymentPlan,repaymentRequest);
            }
        }
    }

    @Override
    public Boolean preRepayment(RepaymentPlan repaymentPlan, String preRequestNo) {
        //1.构造请求数据
        UserAutoPreTransactionRequest userAutoPreTransactionRequest = generateUserAutoPreTransactionRequest(repaymentPlan,preRequestNo);
        //2.请求存管代理服务
        RestResponse<String> restResponse = depositoryAgentApiAgent.userAutoPreTransaction(userAutoPreTransactionRequest);
        //3.返回结果
        return DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(restResponse.getResult());
    }

    @Override
    @Transactional
    public Boolean confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        //1.更新还款明细：已同步
        String preRequestNo = repaymentRequest.getPreRequestNo();
        UpdateWrapper<RepaymentDetail> updateWrapper = new UpdateWrapper<RepaymentDetail>().set("STATUS",StatusCode.STATUS_IN.getCode()).eq("REQUEST_NO",preRequestNo);
        repaymentDetailMapper.update(null,updateWrapper);
        //2.1 更新receivable_plan为已收
        //根据还款计划id，查询应收计划
        QueryWrapper<ReceivablePlan> queryWrapper = new QueryWrapper<ReceivablePlan>().eq("REPAYMENT_ID",repaymentPlan.getId());
        List<ReceivablePlan> list = receivablePlanMapper.selectList(queryWrapper);
        for (ReceivablePlan receivablePlan : list) {
            receivablePlan.setReceivableStatus(1);
            //更新
            receivablePlanMapper.updateById(receivablePlan);
            //2.2 保存数据到receivable_detail
            //构造应收明细
            ReceivableDetail receivableDetail = new ReceivableDetail();
            //应收项标识
            receivableDetail.setReceivableId(receivablePlan.getId());
            //实收本息
            receivableDetail.setAmount(receivablePlan.getAmount());
            //实收时间
            receivableDetail.setReceivableDate(LocalDateTime.now());
            //保存投资人应收明细
            receivableDetailMapper.insert(receivableDetail);
        }
        //3.更新还款计划:已还款
        repaymentPlan.setRepaymentStatus("1");
        int rows = planMapper.updateById(repaymentPlan);
        return rows>0;
    }

    @Override
    public void invokeConfirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        RestResponse<String> repaymentResponse = depositoryAgentApiAgent.confirmRepayment(repaymentRequest);
        if (!DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(repaymentResponse.getResult())) {
            throw new RuntimeException("还款失败");
        }
    }

    @Override
    public void sendRepaymentNotify(String date) {
        //1.查询到期的还款计划
        List<RepaymentPlan> planList = selectDueRepayment(date);
        //2.遍历还款计划
        for (RepaymentPlan repaymentPlan : planList) {
            //3.得到还款人的信息
            RestResponse<BorrowDTO> consumerResponse = consumerApiAgent.getBorrowerMobile(repaymentPlan.getConsumerId());
            //4.得到还款人手机号
            String mobile = consumerResponse.getResult().getMobile();
            //5.发送还款短信
            smsService.sendRepaymentNotify(mobile,date,repaymentPlan.getAmount());
        }
    }

    public static void main(String[] args) {
        System.out.println(RepaymentUtil.fixedRepayment(
                new BigDecimal(6000), new BigDecimal(0.00),
                6, new BigDecimal(0.03)));
    }
    //保存还款计划
    public List<RepaymentPlan> saveRepaymentPlan(ProjectDTO projectDTO,EqualInterestRepayment fixedRepayment) {
        System.out.println(projectDTO);
        List<RepaymentPlan> repaymentPlanList = new ArrayList<>();
        //获取每期利息
        Map<Integer,BigDecimal> interestMap = fixedRepayment.getInterestMap();
        //平台收取利息
        Map<Integer,BigDecimal> commissionMap = fixedRepayment.getCommissionMap();
        //获取每期本金
        for (Integer integer : fixedRepayment.getPrincipalMap().keySet()) {
            //还款计划封装数据
            RepaymentPlan repaymentPlan = new RepaymentPlan();
            //标的id
            repaymentPlan.setProjectId(projectDTO.getId());
            //发表人用户标识
            repaymentPlan.setConsumerId(projectDTO.getConsumerId());
            //发标人用户编码
            repaymentPlan.setUserNo(projectDTO.getUserNo());
            //标的编码
            repaymentPlan.setProjectNo(projectDTO.getProjectNo());
            //期数
            repaymentPlan.setNumberOfPeriods(integer);
            //当期还款利息
            repaymentPlan.setInterest(interestMap.get(integer));
            //当期还款本金
            repaymentPlan.setPrincipal(fixedRepayment.getPrincipalMap().get(integer));
            //本息
            repaymentPlan.setAmount(repaymentPlan.getPrincipal().add(repaymentPlan.getInterest()));
            //应还时间: 当前时间+期数(月)
            repaymentPlan.setShouldRepaymentDate(DateUtil.localDateTimeAddMonth(DateUtil.now(),integer));
            //应还状态：当前业务为待还
            repaymentPlan.setRepaymentStatus("0");
            //计划创建时间
            repaymentPlan.setCreateDate(DateUtil.now());
            //设置平台佣金
            repaymentPlan.setCommission(commissionMap.get(integer));
            //保存到为数据库
            planMapper.insert(repaymentPlan);
            repaymentPlanList.add(repaymentPlan);
        }
        return repaymentPlanList;
    }
    //保存应收明细
    public void saveReceivablePlan(RepaymentPlan repaymentPlan,TenderDTO tenderDTO,EqualInterestRepayment receipt) {
        //应收本金
        Map<Integer,BigDecimal> principalMap = receipt.getPrincipalMap();
        //应收利息
        Map<Integer,BigDecimal> interestMap = receipt.getInterestMap();
        //平台收取利息
        Map<Integer,BigDecimal> commissionMap = receipt.getCommissionMap();
        //封装投资人应收明细
        ReceivablePlan receivablePlan = new ReceivablePlan();
        //投标信息标识
        receivablePlan.setTenderId(tenderDTO.getId());
        //设置期数
        receivablePlan.setNumberOfPeriods(repaymentPlan.getNumberOfPeriods());
        //投标人用户标识
        receivablePlan.setConsumerId(tenderDTO.getConsumerId());
        //投标人用户编码
        receivablePlan.setUserNo(tenderDTO.getUserNo());
        //还款计划标识
        receivablePlan.setRepaymentId(repaymentPlan.getId());
        //应收利息
        receivablePlan.setInterest(interestMap.get(repaymentPlan.getNumberOfPeriods()));
        //应收本金
        receivablePlan.setPrincipal(principalMap.get(repaymentPlan.getNumberOfPeriods()));
        //应收本息
        receivablePlan.setAmount(receivablePlan.getInterest().add(receivablePlan.getPrincipal()));
        //应收时间
        receivablePlan.setShouldReceivableDate(repaymentPlan.getShouldRepaymentDate());
        //应收状态
        receivablePlan.setReceivableStatus(0);
        //创建时间
        receivablePlan.setCreateDate(DateUtil.now());
        //投资人让利(佣金)
        receivablePlan.setCommission(commissionMap.get(repaymentPlan.getNumberOfPeriods()));;
        //保存
        receivablePlanMapper.insert(receivablePlan);
    }

    /**
     * 构造存管代理服务预处理请求数据
     * @param repaymentPlan
     * @param preRequestNo
     * @return
     */
    private UserAutoPreTransactionRequest generateUserAutoPreTransactionRequest(RepaymentPlan repaymentPlan,String preRequestNo) {
        //构造请求数据
        UserAutoPreTransactionRequest request = new UserAutoPreTransactionRequest();
        //冻结金额
        request.setAmount(repaymentPlan.getAmount());
        //预处理业务类型
        request.setBizType(PreprocessBusinessTypeCode.REPAYMENT.getCode());
        //标的号
        request.setProjectNo(repaymentPlan.getProjectNo());
        //请求流水号
        request.setRequestNo(preRequestNo);
        //标的用户编码(发标人)
        request.setUserNo(repaymentPlan.getUserNo());
        //关联业务实体标识
        request.setId(repaymentPlan.getId());
        //返回结果
        return request;
    }

    /**
     * 构造还款信息请求数据
     * @param repaymentPlan
     * @param preRequestNo
     * @return
     */
    private RepaymentRequest generateRepaymentRequest(RepaymentPlan repaymentPlan,String preRequestNo) {
        //根据还款计划id，获取应收计划
        QueryWrapper<ReceivablePlan> queryWrapper = new QueryWrapper<ReceivablePlan>().eq("REPAYMENT_ID",repaymentPlan.getId());
        List<ReceivablePlan> receivablePlanList = receivablePlanMapper.selectList(queryWrapper);
        //封装请求数据
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        //还款总额
        repaymentRequest.setAmount(repaymentPlan.getAmount());
        //业务实体id
        repaymentRequest.setId(repaymentPlan.getId());
        //向借款人收取的佣金
        repaymentRequest.setCommission(repaymentPlan.getCommission());
        //标的编码
        repaymentRequest.setProjectNo(repaymentPlan.getProjectNo());
        //请求流水号
        repaymentRequest.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        //预处理业务流水号
        repaymentRequest.setPreRequestNo(preRequestNo);
        //放款明细
        List<RepaymentDetailRequest> detailRequests = new ArrayList<>();
        for (ReceivablePlan receivablePlan : receivablePlanList) {
            RepaymentDetailRequest detailRequest = new RepaymentDetailRequest();
            //投资人用户编码
            detailRequest.setUserNo(receivablePlan.getUserNo());
            //向投资人收取的佣金
            detailRequest.setCommission(receivablePlan.getCommission());
            //无派息
            //投资人应得本金
            detailRequest.setAmount(receivablePlan.getPrincipal());
            //投资人应得利息
            detailRequest.setInterest(receivablePlan.getInterest());
            //添加到集合
            detailRequests.add(detailRequest);
        }
        //还款明细请求信息
        repaymentRequest.setDetails(detailRequests);
        return repaymentRequest;
    }
}
