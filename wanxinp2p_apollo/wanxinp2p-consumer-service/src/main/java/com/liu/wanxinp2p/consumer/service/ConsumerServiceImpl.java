package com.liu.wanxinp2p.consumer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountRegisterDTO;
import com.liu.wanxinp2p.api.consumer.model.*;
import com.liu.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import com.liu.wanxinp2p.api.depository.model.DepositoryReturnCode;
import com.liu.wanxinp2p.api.depository.model.GatewayRequest;
import com.liu.wanxinp2p.common.domain.CodePrefixCode;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.common.domain.StatusCode;
import com.liu.wanxinp2p.common.exception.CommonErrorCode;
import com.liu.wanxinp2p.common.exception.GlobalException;
import com.liu.wanxinp2p.common.util.CodeNoUtil;
import com.liu.wanxinp2p.common.util.IDCardUtil;
import com.liu.wanxinp2p.consumer.agent.AccountServiceAgent;
import com.liu.wanxinp2p.consumer.agent.DepositoryServiceAgent;
import com.liu.wanxinp2p.consumer.entity.BankCard;
import com.liu.wanxinp2p.consumer.entity.Consumer;
import com.liu.wanxinp2p.common.exception.ConsumerErrorCode;
import com.liu.wanxinp2p.consumer.mapper.ConsumerMapper;
import org.dromara.hmily.annotation.Hmily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements ConsumerService{

    public static Logger log = LoggerFactory.getLogger(ConsumerServiceImpl.class);

    @Autowired
    private AccountServiceAgent accountServiceAgent;

    @Autowired
    private BankCardService bankCardService;

    @Autowired
    private DepositoryServiceAgent depositoryServiceAgent;
    /**
     * 校验手机号码，查看是否有这个用户
     * @param mobile 手机号码
     * @return
     */
    @Override
    public Integer checkMobile(String mobile) {
        return getByMobile(mobile,false) == null? 0 : 1;
    }

    /**
     * 注册业务
     * 需要调用Account统一账号服务中的服务，所以需要使用分布式事务
     * @param consumerRegisterDTO 客户登录信息
     */
    @Override
    @Hmily(confirmMethod = "confirmRegister",cancelMethod = "cancelRegister")
    public void register(ConsumerRegisterDTO consumerRegisterDTO) {
        //检测是否已经注册
        if (1 == checkMobile(consumerRegisterDTO.getMobile())) {
            //如果已经注册过了，就抛出用户已存在异常
            throw new GlobalException(ConsumerErrorCode.ERROR_HAVED_USER);
        }
        Consumer consumer = new Consumer();
        //通常情况下，由于前端没有提供username的输入,所以mobile就是username
        BeanUtils.copyProperties(consumerRegisterDTO,consumer);
        //用户编码，在存管系统中唯一标识
        consumer.setUserNo(CodeNoUtil.getNo(CodePrefixCode.CODE_CONSUMER_PREFIX));
        //是否已绑定银行卡
        consumer.setIsBindCard(0);
        //用户角色 C端用户
        consumer.setUserType("c");
        //保存
        save(consumer);
        //进行远程调用
        AccountRegisterDTO accountRegisterDTO = new AccountRegisterDTO();
        BeanUtils.copyProperties(consumerRegisterDTO,accountRegisterDTO);
        //进行远程调用，得到AccountDTO
        RestResponse<AccountDTO> register = accountServiceAgent.register(accountRegisterDTO);
        //判断调用是否成功
        if (register.getCode() != CommonErrorCode.SUCCESS.getCode()) {
            throw new GlobalException(ConsumerErrorCode.ERROR_FAIL_REGISTER);
        }
    }

    /**
     * 生成开户数据
     * 不需要进行分布式事物，因为这个分布式事物只是在原表的基础上修改，以及在存管代理服务上添加一条状态为未同步的记录，事物出错对于业务没啥影响
     * @param consumerRequest
     * @return
     */
    @Override
    public RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest) {
        System.out.println("Creating...........");
        //根据手机号码找到用户
        ConsumerDTO consumerDTO = getByMobile(consumerRequest.getMobile(),false);
        System.out.println(consumerDTO);
        //判断是否开户
        if (consumerDTO.getIsBindCard() == 1) {
            System.out.println("已经开户了");
            throw new GlobalException(ConsumerErrorCode.ERROR_HAVED_ACCOUNT);
        }
        //判断银行卡是否已经被绑定
        BankCardDTO bankCardDTO = bankCardService.getByCardNumber(consumerRequest.getCardNumber());
        //如果当前这张卡已经存在了，并且现在还在使用中(未解绑)
        if (bankCardDTO != null && bankCardDTO.getStatus() == StatusCode.STATUS_IN.getCode()) {
            System.out.println("这张卡正在使用");
            throw new GlobalException(ConsumerErrorCode.ERROR_LOCK_CARD);
        }
        System.out.println(bankCardDTO);
        //更新用户开户信息
        //准备开户所需要的基础数据
        consumerRequest.setId(consumerDTO.getId());
        //产生请求流水号和用户编号
        consumerRequest.setUserNo(consumerDTO.getUserNo());
        consumerRequest.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        System.out.println("consumerRequest:"+consumerRequest);
        //更新
        UpdateWrapper<Consumer> updateWrapper = new UpdateWrapper<Consumer>().eq("MOBILE",consumerDTO.getMobile());
        updateWrapper.set("USER_NO", consumerRequest.getUserNo());
        updateWrapper.set("FULLNAME",consumerRequest.getFullname());
        updateWrapper.set("REQUEST_NO",consumerRequest.getRequestNo());
        updateWrapper.set("ID_NUMBER",consumerRequest.getIdNumber());
        updateWrapper.set("AUTH_LIST","ALL");
        update(updateWrapper);

        //保存用户绑卡信息
        BankCard bankCard = new BankCard();
        bankCard.setConsumerId(consumerDTO.getId());
        bankCard.setBankCode(consumerRequest.getBankCode());
        bankCard.setCardNumber(consumerRequest.getCardNumber());
        bankCard.setMobile(consumerRequest.getMobile());
        //还未和银行同步
        bankCard.setStatus(StatusCode.STATUS_OUT.getCode());
        //有可能这张卡已经存在了，同时这张卡已经被解绑了（没有在使用）,就直接更新信息
        BankCardDTO existBankCard = bankCardService.getByConsumerId(bankCard.getConsumerId());
        if (existBankCard != null) {
            bankCard.setId(existBankCard.getId());
        }
        bankCardService.saveOrUpdate(bankCard);
        return depositoryServiceAgent.createConsumer(consumerRequest);
    }
    /**
     * 更新开户结果
     * @param response 银行存管代理服务发送的DepositoryConsumerResponse消息，里面包含了银行卡的信息、从而更新consumer库中
     *                 bank_card表的银行卡信息
     * @return
     */
    @Override
    @Transactional //同时修改了多个表，所以需要开启事务
    public Boolean modifyResult(DepositoryConsumerResponse response) {
        System.out.println("response:" + response);
        //1.获取银行存管系统响应的状态
        String response_code = response.getRespCode();
        System.out.println("response_code:" + response_code);
        System.out.println(DepositoryReturnCode.RETURN_CODE_00000.equals(response_code));
        //如果状态是成功，则为1，表示已经成功和银行存管系统同步，否则为2，表示和银行同步失败
        int status = DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(response_code) ? StatusCode.STATUS_IN.getCode() : StatusCode.STATUS_FAIL.getCode();
        //2.更新开户结果 更新银行卡绑定状态与用户状态(是否同步)
        Consumer consumer = getByRequestNo(response.getRequestNo());
        System.out.println("theConsumer:"+consumer);
        update(new UpdateWrapper<Consumer>().eq("ID",consumer.getId())
        .set("IS_BIND_CARD",status)
        .set("STATUS",status));
        //3.更新银行卡信息
        System.out.println("consumer:"+consumer);
        QueryWrapper<BankCard> queryWrapper = new QueryWrapper<BankCard>().eq("CONSUMER_ID",consumer.getId());
        BankCard bankCard = bankCardService.getOne(queryWrapper);
        bankCard.setStatus(status);
        bankCard.setBankName(response.getBankName());
        bankCard.setBankCode(response.getBankCode());
        System.out.println("UpdateBankCard:"+bankCard);
        return bankCardService.saveOrUpdate(bankCard);
    }

    /**
     * 私有方法 : 根据电话号码获取用户
     * @param mobile 电话号码
     * @param throwEx MyBatisPlus 参数， 如果为true就直接执行，如果为false就查询列表并返回第一个
     * @return
     */
    @Override
    public ConsumerDTO getByMobile(String mobile, Boolean throwEx) {
        QueryWrapper<Consumer> wrapper = new QueryWrapper<Consumer>().eq("MOBILE", mobile);
        Consumer consumer = getOne(wrapper,throwEx);
        return convertConsumerToConsumerDTO(consumer);
    }

    @Override
    public BorrowDTO getBorrower(Long id) {
        //先找到对应的用户
        Consumer entity = getById(id);
        if (null == entity) {
            log.info("id为{}的用户信息不存在",id);
            throw new GlobalException(ConsumerErrorCode.ERROR_NO_USER);
        }
        ConsumerDTO consumerDTO = convertConsumerToConsumerDTO(entity);
        BorrowDTO borrowDTO = new BorrowDTO();
        BeanUtils.copyProperties(consumerDTO,borrowDTO);
        Map<String,String> cardInfo = IDCardUtil.getInfo(borrowDTO.getIdNumber());
        borrowDTO.setAge(new Integer(cardInfo.get("age")));
        borrowDTO.setBirthday(cardInfo.get("birthday"));
        borrowDTO.setGender(cardInfo.get("gender"));
        return borrowDTO;
    }

    @Override
    public RestResponse<GatewayRequest> createRechargeRecord(RechargeRequest rechargeRequest) {
        log.info("Consumer-Service充值开启");

        return depositoryServiceAgent.createRechargeRecord(rechargeRequest);
    }

    /**
     * 私有方法: 根据请求的流水号来找到Consumer
     * @param requestNo
     * @return
     */
    private Consumer getByRequestNo(String requestNo) {
        return getOne(new QueryWrapper<Consumer>().eq("REQUEST_NO",requestNo));
    }
    /**
     * 转换Consumer对象为ConsumerDTO对象
     * @param consumer
     * @return
     */
    private ConsumerDTO convertConsumerToConsumerDTO(Consumer consumer) {
        if (null == consumer) {
            return null;
        }
        ConsumerDTO consumerDTO = new ConsumerDTO();
        BeanUtils.copyProperties(consumer,consumerDTO);
        return consumerDTO;
    }

    public void confirmRegister(ConsumerRegisterDTO consumerRegisterDTO) {
        log.info("execute confirmRegister");
    }

    public void confirmCreateConsumer(ConsumerRequest consumerRequest) {
        log.info("execute confirmCreateConsumer");
    }


}
