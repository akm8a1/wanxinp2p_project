package com.liu.wanxinp2p.depository.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.wanxinp2p.api.consumer.model.ConsumerRequest;
import com.liu.wanxinp2p.api.depository.model.*;
import com.liu.wanxinp2p.api.transaction.model.ModifyProjectStatusDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.common.domain.CodePrefixCode;
import com.liu.wanxinp2p.common.domain.PreprocessBusinessTypeCode;
import com.liu.wanxinp2p.common.domain.StatusCode;
import com.liu.wanxinp2p.common.exception.GlobalException;
import com.liu.wanxinp2p.common.util.CodeNoUtil;
import com.liu.wanxinp2p.common.util.EncryptUtil;
import com.liu.wanxinp2p.common.util.HttpClientUitl;
import com.liu.wanxinp2p.common.util.RSAUtil;
import com.liu.wanxinp2p.depository.common.cache.RedisCache;
import com.liu.wanxinp2p.depository.common.constant.DepositoryErrorCode;
import com.liu.wanxinp2p.depository.common.constant.DepositoryRequestTypeCode;
import com.liu.wanxinp2p.depository.entity.DepositoryRecord;
import com.liu.wanxinp2p.depository.mapper.DepositoryRecordMapper;
import com.liu.wanxinp2p.depository.message.GatewayMessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DepositoryRecordServiceImpl extends ServiceImpl<DepositoryRecordMapper, DepositoryRecord> implements DepositoryRecordService {

    /**
     * apollo配置
     */
    @Autowired
    private ConfigService config;

    @Autowired
    private DeDuplicationService deDuplicationService;

    @Autowired
    private GatewayMessageProducer gatewayMessageProducer;

    @Autowired
    private RedisCache cache;

    /**
     * 开户
     * @param consumerRequest
     * @return
     */
    @Override
    public GatewayRequest createConsumer(ConsumerRequest consumerRequest) {
        //1.保存交易记录
        //构造交易记录对象
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        depositoryRecord.setRequestNo(consumerRequest.getRequestNo());
        depositoryRecord.setRequestType(DepositoryRequestTypeCode.CONSUMER_CREATE.getCode());
        depositoryRecord.setObjectType("Consumer");
        //就是这个用户的id
        depositoryRecord.setObjectId(consumerRequest.getId());
        depositoryRecord.setCreateDate(LocalDateTime.now());
        //当前交易状态为未同步
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        //保存 p2p平台存储的交易记录
        save(depositoryRecord);
        //2.签名数据并返回
        //返回数据，json格式
        String reqData = JSON.toJSONString(consumerRequest);
        //签名 用私钥进行加密
        String sign = RSAUtil.sign(reqData,config.getP2pPrivateKey(),"utf-8");
        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setServiceName("PERSONAL_REGISTER");
        gatewayRequest.setPlatformNo(config.getP2pCode());
        //使用Base64编码之后再URL编码
        gatewayRequest.setReqData(EncryptUtil.encodeURL(EncryptUtil.encodeUTF8StringBase64(reqData)));
        gatewayRequest.setSignature(EncryptUtil.encodeURL(sign));
        gatewayRequest.setDepositoryUrl(config.getDepositoryUrl()+"/gateway");
        return gatewayRequest;
    }

    /**
     * 根据请求流水号更新请求的状态，最后返回修改的结果(属于事物消息的本地事务，所以需要同时操作deduplication表 )
     * @param requestNo 请求号
     * @param status 响应状态
     * @return
     */
    @Override
    @Transactional
    public Boolean modifyRequestStatus(String requestNo,Integer status) {
        //根据requestNo设置request_status，同时更新confirm_date
        Wrapper<DepositoryRecord> wrapper = new UpdateWrapper<DepositoryRecord>()
                .eq("REQUEST_NO",requestNo)
                .set("REQUEST_STATUS",status)
                .set("CONFIRM_DATE",LocalDateTime.now());
        boolean ans = update(wrapper);
        //事物消息
        deDuplicationService.addTx(requestNo);
        return ans;
    }

    /**
     * 发送开通存管账号之后的Consumer-Service更新消息-发送消息
     */
    @Override
    public void sendCreateConsumerMessage(DepositoryConsumerResponse response) {
        gatewayMessageProducer.sendPersonalRegister(response);
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> createProject(ProjectDTO projectDTO) throws UnsupportedEncodingException {
        System.out.println("操作的ProjectDTO:" + projectDTO);
        //1.保存交易记录
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        //设置请求流水号
        depositoryRecord.setRequestNo(projectDTO.getRequestNo());
        //设置请求类型
        depositoryRecord.setRequestType(DepositoryRequestTypeCode.CREATE.getCode());
        //设置关联的业务实体
        depositoryRecord.setObjectType("Project");
        //设置关联的业务实体标识
        depositoryRecord.setObjectId(projectDTO.getId());
        //设置记录创建时间
        depositoryRecord.setCreateDate(LocalDateTime.now());
        //设置数据同步状态
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        //保存数据
        DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
        if (responseDTO != null) {
            return responseDTO;
        }
        //重新获取DepositoryRecord
        depositoryRecord = getEntityByRequestNo(projectDTO.getRequestNo());
        //2.对数据reqData进行签名并给银行存管系统发送数据
        ProjectRequestDataDTO projectRequestDataDTO = convertProjectDTOToProjectRequestDataDTO(projectDTO);
        //url地址
        String url = config.getDepositoryUrl()+"/service";
        //平台编号
        String platformNo = config.getP2pCode();
        //转换为JSON
        String jsonString = JSON.toJSONString(projectRequestDataDTO);
        //通过base64进行编码
        String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //签名
        String sign = RSAUtil.sign(jsonString,config.getP2pPrivateKey(),"UTF-8");
        //服务名
        String serviceName = "CREATE_PROJECT";
        System.out.println("depositoryRecord:"+depositoryRecord);
        //发送请求
        final DepositoryResponseDTO<DepositoryBaseResponse> depositoryBaseResponseDepositoryResponseDTO = sendHttpGet(serviceName, url, reqData, platformNo, sign, depositoryRecord);
        System.out.println("depositoryBaseResponseDepositoryResponseDTO"+depositoryBaseResponseDepositoryResponseDTO);
        return depositoryBaseResponseDepositoryResponseDTO;
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> userAutoPreTransaction(UserAutoPreTransactionRequest userAutoPreTransactionRequest) throws UnsupportedEncodingException {
        DepositoryRecord depositoryRecord = new DepositoryRecord(
                userAutoPreTransactionRequest.getRequestNo(),
                userAutoPreTransactionRequest.getBizType(),
                "UserAutoPreTransactionRequest",
                userAutoPreTransactionRequest.getId()
        );
        //实现幂等性
        DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
        if (responseDTO != null) {
            return responseDTO;
        }
        //根据requestNo获取交易记录
        depositoryRecord = getEntityByRequestNo(userAutoPreTransactionRequest.getRequestNo());
        //向存管系统发送数据
        String jsonString = JSON.toJSONString(userAutoPreTransactionRequest);
        //业务报文数据，通过BASE64进行编码处理
        String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //发送请求，获取结果
        //凭借银行存管系统地址
        String url = config.getDepositoryUrl() + "/service";
        //签名
        String sign = RSAUtil.sign(jsonString,config.getP2pPrivateKey(),"UTF-8");
        //服务名
        String serviceName = "USER_AUTO_PRE_TRANSACTION";
        //平台编码
        String platform_code = config.getP2pCode();
        //发送请求
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryBaseResponseDepositoryResponseDTO = sendHttpGet(serviceName, url, reqData, platform_code, sign, depositoryRecord);
        return depositoryBaseResponseDepositoryResponseDTO;
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> confirmLoan(LoanRequest loanRequest) throws UnsupportedEncodingException {
        DepositoryRecord depositoryRecord = new DepositoryRecord(loanRequest.getRequestNo(),DepositoryRequestTypeCode.FULL_LOAN.getCode(),"LoanRequest",loanRequest.getId());
        //实现幂等性
        DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
        if (responseDTO != null) {
            return responseDTO;
        }
        //根据requestNo获取交易记录
        depositoryRecord = getEntityByRequestNo(loanRequest.getRequestNo());
        //loanRequest转化为jsonString
        String jsonString = JSON.toJSONString(loanRequest);
        //业务数据报文，使用Base64编码
        String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //银行存管系统请求地址
        String url = config.getDepositoryUrl() + "/service";
        //服务名
        String serviceName = "CONFIRM_LOAN";
        //平台编码
        String platform_code = config.getP2pCode();
        //签名
        String sign = RSAUtil.sign(jsonString,config.getP2pPrivateKey(),"UTF-8");
        //请求银行存管系统并返回
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryBaseResponseDepositoryResponseDTO = sendHttpGet(serviceName, url, reqData, platform_code, sign, depositoryRecord);
        return depositoryBaseResponseDepositoryResponseDTO;
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> modifyProjectStatus(ModifyProjectStatusDTO modifyProjectStatusDTO) throws UnsupportedEncodingException {

        DepositoryRecord depositoryRecord = new DepositoryRecord(
                CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX),
                DepositoryRequestTypeCode.MODIFY_STATUS.getCode(),
                "Project",
                modifyProjectStatusDTO.getId()
        );
        //实现幂等性
        DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
        System.out.println(responseDTO);
        if (null != responseDTO) {
            return responseDTO;
        }
        //根据requestNo获取交易记录
        depositoryRecord = getEntityByRequestNo(modifyProjectStatusDTO.getRequestNo());
        String jsonString = JSON.toJSONString(modifyProjectStatusDTO);
        //业务数据报文 需要加密
        String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //拼接银行存管系统请求地址
        String url = config.getDepositoryUrl() + "/service";
        //服务名
        String serviceName = "MODIFY_PROJECT";
        //平台编码
        String platform_code = config.getP2pCode();
        //签名
        String sign = RSAUtil.sign(jsonString,config.getP2pPrivateKey(),"UTF-8");
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryBaseResponseDepositoryResponseDTO = sendHttpGet(serviceName, url, reqData, platform_code, sign, depositoryRecord);
        return depositoryBaseResponseDepositoryResponseDTO;
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> confirmRepayment(RepaymentRequest repaymentRequest) {
        try{
            //构造交易记录
            DepositoryRecord depositoryRecord = new DepositoryRecord(
                    repaymentRequest.getRequestNo(),
                    PreprocessBusinessTypeCode.REPAYMENT.getCode(),
                    "Repayment",
                    repaymentRequest.getId()
            );
            //分布式事务幂等性实现
            DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
            if (responseDTO != null) {
                return responseDTO;
            }
            //获取最新交易记录
            depositoryRecord = getEntityByRequestNo(repaymentRequest.getRequestNo());
            /**
             * 确认还款(调用银行存管系统)
             */
            String jsonString = JSON.toJSONString(repaymentRequest);
            //业务数据报文
            String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
            //银行存管系统请求地址
            String url = config.getDepositoryUrl() +"/service";
            //服务名
            String serviceName = "CONFIRM_REPAYMENT";
            //平台编码
            String platform_code = config.getP2pCode();
            //签名
            String sign = RSAUtil.sign(jsonString,config.getP2pPrivateKey(),"UTF-8");
            //发送请求
            DepositoryResponseDTO<DepositoryBaseResponse> depositoryBaseResponseDepositoryResponseDTO = sendHttpGet(serviceName, url, reqData, platform_code, sign, depositoryRecord);
            return depositoryBaseResponseDepositoryResponseDTO;
        } catch (Exception e) {
            throw new GlobalException(DepositoryErrorCode.E_160101);
        }
    }

    private DepositoryResponseDTO<DepositoryBaseResponse> sendHttpGet(String serviceName,String url,String reqData,String platformNo,String sign,DepositoryRecord depositoryRecord) throws UnsupportedEncodingException {
        String encode = "UTF-8";
        Map<String,String> params = new HashMap<String,String>();
        params.put("serviceName",serviceName);
        params.put("platformNo",platformNo);
        params.put("reqData",reqData);
        params.put("signature",sign);
        String response = HttpClientUitl.doGet(url, params, encode);
        System.out.println("response:"+response);
        JSONObject parseObject = JSON.parseObject(response);
        //获取业务报文,需要解码
        String respData = parseObject.getString("respData");
        System.out.println("respData:"+respData);
        //获取签名
        String signature = parseObject.getString("signature");
        //验证签名
        if (!RSAUtil.verify(respData,signature,config.getDepositoryPublicKey(),encode)) {
            //验证失败就修改signature,从而判断业务是否正常
            parseObject.put("signature","false");
        }
        String resp= JSONObject.toJSONString(parseObject);
        System.out.println("resp:" + resp);
        JSONObject object = JSON.parseObject(resp);
        System.out.println("object:"+object);
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = new DepositoryResponseDTO<>();
        depositoryResponse.setSignature(object.getObject("signature", String.class));
        depositoryResponse.setRespData(object.getObject("respData", DepositoryBaseResponse.class));
        depositoryRecord.setResponseData(response);
        System.out.println("depositoryResponse:"+depositoryResponse);
        if (!"false".equals(depositoryResponse.getSignature())) {
            //成功，设置数据同步状态
            depositoryRecord.setRequestStatus(StatusCode.STATUS_IN.getCode());
            //设置消息确认时间
            depositoryRecord.setConfirmDate(LocalDateTime.now());
            //更新数据库
            updateById(depositoryRecord);
        } else {
            //失败 - 设置数据同步状态
            depositoryRecord.setRequestStatus(StatusCode.STATUS_FAIL.getCode());
            //设置消息确认时间
            depositoryRecord.setConfirmDate(LocalDateTime.now());
            //更新数据库
            updateById(depositoryRecord);
            throw new GlobalException(DepositoryErrorCode.E_160101);
        }
        return depositoryResponse;
    }

    /**
     * 实现幂等性
     * @param depositoryRecord
     * @return
     */
    private DepositoryResponseDTO<DepositoryBaseResponse> handleIdempotent(DepositoryRecord depositoryRecord) {
        //根据requestNo查询交易记录
        String requestNo = depositoryRecord.getRequestNo();
        DepositoryRecordDTO record = getByRequestNo(requestNo);
        //1.交易记录不存在则新增交易记录
        if (record == null) {
            System.out.println("交易记录不存在");
            depositoryRecord.setCreateDate(LocalDateTime.now());
            save(depositoryRecord);
            return null;
        }
        //2.交易记录存在并且数据状态为非同步,说明这条数据被存储在了存管代理数据库中，
        // 但是数据可能还没有发送给银行存管系统或者银行存管系统在接收到数据并处理之后还没有返回响应
        //这个阶段是有并发可能性的，可能会造成重复给存管系统发送消息,在这里也要进行处理
        if (StatusCode.STATUS_OUT.getCode() == depositoryRecord.getRequestStatus()) {
            System.out.println("进入并发可能区域，当前状态为:"+depositoryRecord.getRequestStatus());
            //通过分布式锁来实现幂等性
            //给锁设置10s的过期时间
            if (cache.setnx(requestNo,String.valueOf(1),10, TimeUnit.SECONDS)) {
                //如果获得锁成功了，就由这个请求去处理
                return null;
            } else {
                //说明已经有其他请求去处理了，直接抛出异常就行:交易正在执行
                throw new GlobalException(DepositoryErrorCode.E_160103);
            }
        }
        //3.如果交易记录已经存在，同时状态为已同步，那等待返回处理结果就可以了
        System.out.println("交易记录已存在");
        return JSON.parseObject(depositoryRecord.getResponseData(), DepositoryResponseDTO.class);
    }

    private ProjectRequestDataDTO convertProjectDTOToProjectRequestDataDTO(ProjectDTO projectDTO) {
        if (projectDTO == null) {
            return null;
        }
        ProjectRequestDataDTO requestDataDTO = new ProjectRequestDataDTO();
        BeanUtils.copyProperties(projectDTO,requestDataDTO);
        requestDataDTO.setProjectAmount(projectDTO.getAmount());
        requestDataDTO.setProjectName(projectDTO.getName());
        requestDataDTO.setProjectDesc(projectDTO.getDescription());
        requestDataDTO.setProjectType(projectDTO.getType());
        requestDataDTO.setProjectPeriod(projectDTO.getPeriod());
        return requestDataDTO;
    }

    private DepositoryRecordDTO getByRequestNo(String requestNo) {
        DepositoryRecord depositoryRecord = getEntityByRequestNo(requestNo);
        System.out.println("查询:"+depositoryRecord);
        if (depositoryRecord == null) {
            return null;
        }
        DepositoryRecordDTO depositoryRecordDTO = new DepositoryRecordDTO();
        BeanUtils.copyProperties(depositoryRecord,depositoryRecordDTO);
        return depositoryRecordDTO;
    }

    /**
     * 根据requestNo得到DepositoryRecord
     * @param requestNo
     * @return
     */
    private DepositoryRecord getEntityByRequestNo(String requestNo) {
        Wrapper<DepositoryRecord> wrapper = new QueryWrapper<DepositoryRecord>().eq("REQUEST_NO",requestNo);
        return getOne(wrapper);
    }

    public static void main(String[] args) {
        String resp = "{\"signature\":\"hY2ZdRTx+Lhlq9ixm7iOcGK39a4v1SyKHk7J0DfDla1xZMMVaiMjt9Ai6euWISewsXX8TcbIvyGblNv0d/ivmw==\",\"respData\":{\"respMsg\":\"成功\",\"requestNo\":\"REQ_D30BF3A0D3B34700BBF1F9CEA814CF6B\",\"respCode\":\"00000\",\"status\":1}}";
        System.out.println(resp);
        JSONObject object = JSON.parseObject(resp);
        System.out.println("object:"+object);
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = new DepositoryResponseDTO<>();
        depositoryResponse.setSignature(object.getObject("signature", String.class));
        depositoryResponse.setRespData(object.getObject("respData", DepositoryBaseResponse.class));
        System.out.println(depositoryResponse);

    }
}
