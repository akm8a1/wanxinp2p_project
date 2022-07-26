package com.liu.wanxinp2p.transaction.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.wanxinp2p.api.account.model.LoginUser;
import com.liu.wanxinp2p.api.consumer.model.BalanceDetailsDTO;
import com.liu.wanxinp2p.api.consumer.model.ConsumerDTO;
import com.liu.wanxinp2p.api.depository.model.DepositoryReturnCode;
import com.liu.wanxinp2p.api.depository.model.LoanDetailRequest;
import com.liu.wanxinp2p.api.depository.model.LoanRequest;
import com.liu.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import com.liu.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import com.liu.wanxinp2p.api.transaction.model.*;
import com.liu.wanxinp2p.common.domain.*;
import com.liu.wanxinp2p.common.domain.CodePrefixCode;
import com.liu.wanxinp2p.common.exception.CommonErrorCode;
import com.liu.wanxinp2p.common.exception.GlobalException;
import com.liu.wanxinp2p.common.util.CodeNoUtil;
import com.liu.wanxinp2p.common.util.CommonUtil;
import com.liu.wanxinp2p.transaction.agent.ConsumerServiceAgent;
import com.liu.wanxinp2p.transaction.agent.ContentSearchApiAgent;
import com.liu.wanxinp2p.transaction.agent.DepositoryAgentService;
import com.liu.wanxinp2p.transaction.common.constant.ProjectCode;
import com.liu.wanxinp2p.transaction.common.constant.RepaymentWayCode;
import com.liu.wanxinp2p.transaction.common.constant.TradingCode;
import com.liu.wanxinp2p.transaction.common.constant.TransactionErrorCode;
import com.liu.wanxinp2p.transaction.common.utils.IncomeCalcUtil;
import com.liu.wanxinp2p.transaction.common.utils.SecurityUtil;
import com.liu.wanxinp2p.transaction.entity.Project;
import com.liu.wanxinp2p.transaction.entity.Tender;
import com.liu.wanxinp2p.transaction.mapper.ProjectMapper;
import com.liu.wanxinp2p.transaction.mapper.TenderMapper;
import com.liu.wanxinp2p.transaction.message.P2PTransactionProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService{

    @Autowired
    private ConsumerServiceAgent consumerServiceAgent;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DepositoryAgentService depositoryAgentService;

    @Autowired
    private ContentSearchApiAgent contentSearchApiAgent;

    @Autowired
    private TenderMapper tenderMapper;

    @Autowired
    private P2PTransactionProducer p2PTransactionProducer;

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        RestResponse<ConsumerDTO> consumer = consumerServiceAgent.getCurrentConsumer(SecurityUtil.getUser().getMobile());
        //设置用户编码
        projectDTO.setUserNo(consumer.getResult().getUserNo());
        //设置用户id
        projectDTO.setConsumerId(consumer.getResult().getId());
        //生成标的编码
        projectDTO.setProjectNo(CodeNoUtil.getNo(CodePrefixCode.CODE_PROJECT_PREFIX));
        //修改标的状态
        projectDTO.setProjectStatus(ProjectCode.COLLECTING.getCode());
        //修改标的可用状态
        projectDTO.setStatus(StatusCode.STATUS_OUT.getCode());
        //设置标的创建时间
        projectDTO.setCreateDate(LocalDateTime.now());
        //设置还款方式(等额本息)
        projectDTO.setRepaymentWay(RepaymentWayCode.FIXED_REPAYMENT.getCode());
        //设置金额
        //设置标的类型
        projectDTO.setType("NEW");
        Project project = convertProjectDTOToEntity(projectDTO);
        //设置利率
        project.setBorrowerAnnualRate(configService.getBorrowerAnnualRate());
        //年化利率(借款人视图)
        project.setAnnualRate(configService.getAnnualRate());
        //年化利率(平台佣金、利差)
        project.setCommissionAnnualRate(configService.getCommissionAnnualRate());
        //债权转让
        project.setIsAssignment(0);
        //设置标的名称: 姓名+性别+第N次借款
        String sex = Integer.parseInt(consumer.getResult().getIdNumber().substring(16,17)) % 2 ==0? "女士":"先生";
        //借款次数查询
        QueryWrapper<Project> wrapper = new QueryWrapper<Project>().eq("CONSUMER_ID",consumer.getResult().getId());
        int count = count(wrapper);
        project.setName(consumer.getResult().getFullname()+sex+"第"+(count+1)+"次借款");
        save(project);
        //设置主键
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        return projectDTO;
    }

    @Override
    public PageVO<ProjectDTO> queryProjectsByQueryDTO(ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy) {
        //条件构造
        QueryWrapper<Project> queryWrapper = new QueryWrapper<Project>();
        //标的类型
        if (StringUtils.isNotBlank(projectQueryDTO.getType())) {
            queryWrapper = queryWrapper.eq("TYPE", projectQueryDTO.getType());
        }
        //起止年化利率
        if (null != projectQueryDTO.getStartAnnualRate()) {
            queryWrapper = queryWrapper.ge("ANNUAL_RATE",projectQueryDTO.getStartAnnualRate());
        }
        if (null != projectQueryDTO.getEndAnnualRate()) {
            queryWrapper = queryWrapper.le("ANNUAL_RATE",projectQueryDTO.getEndAnnualRate());
        }
        //借款期限 -- 区间
        if (null != projectQueryDTO.getStartPeriod()) {
            queryWrapper = queryWrapper.ge("PERIOD",projectQueryDTO.getStartPeriod());
        }
        if (null != projectQueryDTO.getEndPeriod()) {
            queryWrapper = queryWrapper.le("PERIOD",projectQueryDTO.getEndPeriod());
        }
        //标的状态
        if (StringUtils.isNotBlank(projectQueryDTO.getProjectStatus())) {
            queryWrapper = queryWrapper.eq("PROJECT_STATUS",projectQueryDTO.getProjectStatus());
        }
        //构造分页对象
        Page<Project> page = new Page<>(pageNo,pageSize);
        //处理排序 order值： desc或者asc
        if (StringUtils.isNotBlank(order) && StringUtils.isNotBlank(sortBy)) {
            if (order.toLowerCase().equals("asc")) {
                queryWrapper.orderByAsc(sortBy);
            } else if (order.toLowerCase().equals("desc")) {
                queryWrapper.orderByDesc(sortBy);
            }
        } else {
            //默认按照发标时间倒序排序
            queryWrapper.orderByDesc("CREATE_DATE");
        }
        //执行查询
        IPage<Project> projectIPage = page(page, queryWrapper);
        //ENTITY转换为DTO
        List<ProjectDTO> dtoList = convertProjectEntityToDTO(projectIPage.getRecords());
        //封装结果集
        return new PageVO<>(dtoList,projectIPage.getTotal(),pageNo,pageSize);
    }

    @Override
    public String projectsApprovalStatus(Long id, String approveStatus) {
        //1.根据id查询出标的信息并转换为ProjectDTO对象
        Project project = getById(id);
        ProjectDTO projectDTO = convertProjectToDTO(project);
        //2.生成请求流水号
        if (StringUtils.isBlank(project.getRequestNo())) {
            projectDTO.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
            UpdateWrapper<Project> wrapper = new UpdateWrapper<Project>()
                    .set("REQUEST_NO",projectDTO.getRequestNo())
                    .eq("ID", id);
            update(wrapper);
        }
        //3.调用存管代理服务同步标的信息
        RestResponse<String> response = depositoryAgentService.createProject(projectDTO);
        System.out.println("response:"+response);
        if (DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(response.getResult())) {
            //4.修改状态为:已发布
            UpdateWrapper<Project> wrapper = new UpdateWrapper<Project>()
                    .set("STATUS",Integer.parseInt(approveStatus))
                    .set("MODIFY_DATE",LocalDateTime.now())
                    .eq("ID", id);
            update(wrapper);
            return "success";
        }
        //5.如果失败就抛出一个业务异常
        throw new GlobalException(TransactionErrorCode.E_150113);
    }

    @Override
    public PageVO<ProjectDTO> queryProjects(ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy) {
        RestResponse<PageVO<ProjectDTO>> esResponse = contentSearchApiAgent.queryProjectIndex(projectQueryDTO,pageNo,pageSize,sortBy,order);
        if (!esResponse.isSuccessful()) {
            throw new GlobalException(CommonErrorCode.ERROR_UNKNOWN);
        }
        return esResponse.getResult();
    }

    @Override
    public List<ProjectDTO> queryProjectsIds(String ids) {
        //1.构造查询条件
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        List<Long> list = new ArrayList<>();
        for (String s : ids.split(",")) {
            list.add(Long.parseLong(s));
        }
        queryWrapper.in("ID",list);
        //2.执行查询
        List<Project> projects = list(queryWrapper);
        List<ProjectDTO> dtos = new ArrayList<>();
        //3.实体转化为DTO并封装信息
        for (Project project : projects) {
            //实体转换为DTO
            ProjectDTO projectDTO = convertProjectToDTO(project);
            //封装剩余额度
            projectDTO.setRemainingAmount(getProjectRemainingAmount(project));
            //封装标的已投记录数
            projectDTO.setTenderCount(tenderMapper.selectCount(new QueryWrapper<Tender>().eq("PROJECT_ID",project.getId())));
            dtos.add(projectDTO);
        }

        return dtos;
    }

    @Override
    public List<TenderOverviewDTO> queryTendersByProjectId(Long id) {
        List<Tender> tenders = tenderMapper.selectList(new QueryWrapper<Tender>().eq("PROJECT_ID",id));
        List<TenderOverviewDTO> tenderOverviewDTOS = new ArrayList<>();
        for (Tender tender : tenders) {
            TenderOverviewDTO dto = new TenderOverviewDTO();
            BeanUtils.copyProperties(tender,dto);
            dto.setConsumerUsername(CommonUtil.hiddenMobile(dto.getConsumerUsername()));
            tenderOverviewDTOS.add(dto);
        }
        return tenderOverviewDTOS;
    }

    @Override
    public TenderDTO createTender(ProjectInvestDTO projectInvestDTO) {
        //1.前置条件判断
        //1.1 判断投标金额是否大于最小投标金额
        BigDecimal amount = new BigDecimal(projectInvestDTO.getAmount());
        //获得最小投标金额
        BigDecimal minInvestmentAmount = configService.getMiniInvestmentAmount();
        if (amount.compareTo(minInvestmentAmount) < 0) {
            throw new GlobalException(TransactionErrorCode.E_150109);
        }
        //1.2 判断用户账户余额是否足够
        //得到当前登录用户
        LoginUser user = SecurityUtil.getUser();
        //通过手机号查询用户信息
        RestResponse<ConsumerDTO> restResponse = consumerServiceAgent.getCurrentConsumer(user.getMobile());
        //通过用户编号查询账户余额
        RestResponse<BalanceDetailsDTO> balanceDetailsDTORestResponse = consumerServiceAgent.getBalance(restResponse.getResult().getUserNo());
        BigDecimal myBalance = balanceDetailsDTORestResponse.getResult().getBalance();
        if (myBalance.compareTo(amount) < 0) {
            throw new GlobalException(TransactionErrorCode.E_150112);
        }
        //1.3 判断标的是否满标，标的状态为FULLY就标识满标
        Project project = getById(projectInvestDTO.getId());
        if (project.getProjectStatus().equalsIgnoreCase("FULLY")) {
            throw new GlobalException(TransactionErrorCode.E_150114);
        }
        //1.4 判断投标金额是否超出剩余未投金额
        BigDecimal remainingAmount = getProjectRemainingAmount(project);
        if (amount.compareTo(remainingAmount) < 1) {
            //1.5 判断此次投标之后的剩余未投金额是否满足最小投标金额
            BigDecimal substract = remainingAmount.subtract(amount);
            int result = substract.compareTo(configService.getMiniInvestmentAmount());
            if (result < 0) {
                if (substract.compareTo(new BigDecimal("0.00")) != 0) {
                    throw new GlobalException(TransactionErrorCode.E_150111);
                }
            }
            //2.保存投标信息并发送给存管代理服务
            //2.1 保存投标信息，数据状态为：未发布
            //封装投标信息
            Tender tender = new Tender();
            //投资人投标金额
            tender.setAmount(amount);
            //投资人用户标识
            tender.setConsumerId(restResponse.getResult().getId());
            tender.setConsumerUsername(restResponse.getResult().getUsername());
            //投标人用户编码
            tender.setUserNo(restResponse.getResult().getUserNo());
            //标的标识
            tender.setProjectId(projectInvestDTO.getId());
            //标的编码
            tender.setProjectNo(project.getProjectNo());
            //标的状态
            tender.setTenderStatus(TradingCode.FROZEN.getCode());
            //创建时间
            tender.setCreateDate(LocalDateTime.now());
            //请求流水号
            tender.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
            //可用状态
            tender.setStatus(0);
            tender.setProjectNo(project.getName());
            //标的期限(单位:天)
            tender.setProjectPeriod(project.getPeriod());
            //年化利率(投资人视图)
            tender.setProjectAnnualRate(project.getAnnualRate());
            //保存到数据库
            tenderMapper.insert(tender);
            //2.2发送数据给存管代理服务
            //构造请求数据
            UserAutoPreTransactionRequest userAutoPreTransactionRequest = new UserAutoPreTransactionRequest();
            //冻结金额
            userAutoPreTransactionRequest.setAmount(amount);
            //预处理业务类型
            userAutoPreTransactionRequest.setBizType(PreprocessBusinessTypeCode.TENDER.getCode());
            //标的号
            userAutoPreTransactionRequest.setProjectNo(project.getProjectNo());
            //请求流水号
            userAutoPreTransactionRequest.setRequestNo(tender.getRequestNo());
            //请求用户名
            userAutoPreTransactionRequest.setUserNo(restResponse.getResult().getUserNo());
            //设置关联业务实体标识
            userAutoPreTransactionRequest.setId(tender.getId());
            //远程调用存管代理服务
            System.out.println(userAutoPreTransactionRequest);
            RestResponse<String> response = depositoryAgentService.userAutoPreTransaction(userAutoPreTransactionRequest);
            //3.根据结果更新投标状态
            //3.1 判断结果
            System.out.println("response:"+response);
            if (DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(response.getResult())) {
                //3.2 修改状态未已发布
                tender.setStatus(1); //状态码为1
                tenderMapper.updateById(tender);
                //3.3 投标成功之后判断是否已经满标
                BigDecimal remainAmount = getProjectRemainingAmount(project);
                if (remainAmount.compareTo(new BigDecimal(0)) == 0) {
                    project.setProjectStatus(ProjectCode.FULLY.getCode());
                    project.setModifyDate(LocalDateTime.now());
                    updateById(project);
                }
                //3.4 转换为dto对象并封装数据
                TenderDTO tenderDTO = convertTenderEntityToDTO(tender);
                //封装标的信息
                project.setRepaymentWay(RepaymentWayCode.FIXED_REPAYMENT.getCode());
                tenderDTO.setProject(convertProjectToDTO(project));
                //封装预期收益
                //根据标的期限计算还款月数
                Double ceil = Math.ceil(project.getPeriod()/30.0);
                Integer month = ceil.intValue();
                System.out.println(projectInvestDTO.getAmount() + ":" + configService.getAnnualRate() + ":" + month);
                //计算预期收益
                tenderDTO.setExpectedIncome(IncomeCalcUtil.getIncomeTotalInterest(new BigDecimal(projectInvestDTO.getAmount()),configService.getAnnualRate(),month));
                return tenderDTO;
            } else {
                //抛出一个业务异常
                log.warn("投标失败！标的ID为：{},存管代理服务返回的状态为:{}",projectInvestDTO.getId(),restResponse.getResult());
                throw new GlobalException(TransactionErrorCode.E_150113);
            }
        } else {
            throw new GlobalException(TransactionErrorCode.E_150110);
        }
    }

    public static void main(String[] args) {
        BigDecimal invest = new BigDecimal(1500);
        BigDecimal yearRate = new BigDecimal(0.00);
        int month = 12;
        System.out.println(IncomeCalcUtil.getIncomeTotalInterest(invest, yearRate, month));
    }
    @Override
    //Spring事务，遇到GlobalException的时候也会回滚
    @Transactional(rollbackFor = GlobalException.class)
    public String loansApprovalStatus(Long id, String approveStatus, String commission) {
        //1.生成放款明细
        //获取标的信息
        Project project = getById(id);
        //构造查询参数，获取这个标的所有投标信息
        Wrapper<Tender> queryWrapper = new QueryWrapper<Tender>().eq("PROJECT_ID",id);
        List<Tender> tenderList = tenderMapper.selectList(queryWrapper);
        //生成还款明细
        LoanRequest loanRequest = generateLoanRequest(project,tenderList,commission);
        //2.放款
        //请求存管代理服务
        RestResponse<String> restResponse = depositoryAgentService.confirmLoan(loanRequest);
        System.out.println("restResponse:" + restResponse);
        if (DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(restResponse.getResult())) {
            //响应成功,更新投标信息
            //投标状态更改为已放款
            updateTenderStatusAlreadyLoan(tenderList);
            //3：修改标的业务状态
            //调用存管代理服务，修改状态为还款中
            //构造请求参数
            ModifyProjectStatusDTO modifyProjectStatusDTO = new ModifyProjectStatusDTO();
            //业务实体id
            modifyProjectStatusDTO.setId(project.getId());
            //业务状态
            modifyProjectStatusDTO.setProjectStatus(ProjectCode.REPAYING.getCode());
            //标的项目编号
            modifyProjectStatusDTO.setProjectNo(project.getProjectNo());
            //执行请求
            System.out.println("测试结果:"+modifyProjectStatusDTO);
            RestResponse<String> modifyProjectProjectstatus = depositoryAgentService.modifyProjectStatus(modifyProjectStatusDTO);
            System.out.println("modifyProjectProjectstatus:"+modifyProjectProjectstatus);
            if (DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(modifyProjectProjectstatus.getResult())) {
                //如果处理成功，就修改标的状态为还款中
                project.setProjectStatus(ProjectCode.REPAYING.getCode());
                updateById(project);
                //4.启动还款
                //封装调用还款服务请求对象的数据
                ProjectWithTendersDTO projectWithTendersDTO = new ProjectWithTendersDTO();
                //封装标的信息
                projectWithTendersDTO.setProject(convertProjectToDTO(project));
                //封装投标信息
                projectWithTendersDTO.setTenders(convertTenderEntityListToDTOList(tenderList));;
                //封装投资人让利
                projectWithTendersDTO.setCommissionInvestorAnnualRate(configService.getCommissionInvestorAnnualRate());
                //封装借款人让利
                projectWithTendersDTO.setCommissionBorrowerAnnualRate(configService.getCommissionBorrowerAnnualRate());
                //调用还款服务，启动还款(生成还款计划，应收明细)
                p2PTransactionProducer.updateProjectStatusAndStartRepayment(project,projectWithTendersDTO);
                return "审核成功";
            } else {
                //失败抛出一个业务异常
                log.warn("审核满标放款失败!标的ID为:{},存管代理服务返回的状态为:{}",project.getId(),restResponse.getResult());
                throw new GlobalException(TransactionErrorCode.E_150113);
            }
        } else {
            //失败抛出一个异常
            log.warn("审核满标放款失败!标的ID为:{},存管代理服务返回的状态为:{}",project.getId(),restResponse.getResult());
            throw new GlobalException(TransactionErrorCode.E_150113);
        }
    }

    /**
     * 执行本地事务，标的状态更新为还款中
     * @param project
     * @return
     */
    @Override
    @Transactional(rollbackFor = GlobalException.class)
    public Boolean updateProjectStatusAndStartRepayment(Project project) {
        project.setProjectStatus(ProjectCode.REPAYING.getCode());
        return updateById(project);
    }

    /**
     * 获取标的剩余可投额度
     * @param project
     * @return
     */
    private BigDecimal getProjectRemainingAmount(Project project) {
        //根据标的id在投标表查询已投额度
        List<BigDecimal> decimals = tenderMapper.selectAmountInvestedByProjectId(project.getId());
        BigDecimal result = new BigDecimal(0);
        for (BigDecimal decimal : decimals) {
            System.out.println(decimal);
            result = result.add(decimal);
        }
        System.out.println("result:"+result);
        //得到剩余额度
        return project.getAmount().subtract(result);
    }
    private List<ProjectDTO> convertProjectEntityToDTO(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        List<ProjectDTO> dtoList = new ArrayList<>();
        for (Project project : projects) {
            ProjectDTO projectDTO = new ProjectDTO();
            BeanUtils.copyProperties(project,projectDTO);
            dtoList.add(projectDTO);
        }
        return dtoList;
    }

    private ProjectDTO convertProjectToDTO(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project,dto);
        return dto;
    }

    private Project convertProjectDTOToEntity(ProjectDTO projectDTO) {
        if (projectDTO == null) {
            return null;
        }
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO,project);
        return project;
    }

    private TenderDTO convertTenderEntityToDTO(Tender tender) {
        if (tender == null) {
            return null;
        }
        TenderDTO tenderDTO = new TenderDTO();
        BeanUtils.copyProperties(tender,tenderDTO);
        return tenderDTO;
    }

    /**
     * 根据标的及投标信息生成放款明细
     * @param project
     * @param tenders
     * @return
     */
    public LoanRequest generateLoanRequest(Project project,List<Tender> tenders,String commission) {
        LoanRequest loanRequest = new LoanRequest();
        //设置标的id
        loanRequest.setId(project.getId());
        //设置平台佣金
        if (StringUtils.isNotBlank(commission)) {
            loanRequest.setCommission(new BigDecimal(commission));
        }
        //设置标的编码
        loanRequest.setProjectNo(project.getProjectNo());
        //设置请求流水号
        loanRequest.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        //处理放款明细
        List<LoanDetailRequest> details = new ArrayList<>();
        for (Tender tender : tenders) {
            LoanDetailRequest loanDetailRequest = new LoanDetailRequest();
            //设置放款金额
            loanDetailRequest.setAmount(tender.getAmount());
            //设置预处理业务流水号
            loanDetailRequest.setPreRequestNo(tender.getRequestNo());
            details.add(loanDetailRequest);
        }
        //设置放款明细
        loanRequest.setDetails(details);
        //返回封装好的数据
        return loanRequest;
    }

    /**
     * 修改投标状态
     * @param tenderList
     */
    private void updateTenderStatusAlreadyLoan(List<Tender> tenderList) {
        for (Tender tender : tenderList) {
            //设置状态为已放款
            tender.setTenderStatus(TradingCode.LOAN.getCode());
            //更新数据库
            tenderMapper.updateById(tender);
        }
    }

    private List<TenderDTO> convertTenderEntityListToDTOList(List<Tender> records) {
        if (records == null) {
            return null;
        }
        List<TenderDTO> dtoList = new ArrayList<>();
        for (Tender record : records) {
            TenderDTO tenderDTO = new TenderDTO();
            BeanUtils.copyProperties(record,tenderDTO);
            dtoList.add(tenderDTO);
        }
        return dtoList;
    }
}
