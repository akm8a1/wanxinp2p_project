package com.liu.wanxinp2p.account.service;

import com.alibaba.fastjson.JSONObject;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.common.domain.Sms;
import com.liu.wanxinp2p.common.domain.VerifyMessage;
import com.liu.wanxinp2p.common.util.HttpClientUitl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 手机验证码服务
 */
@Service
public class SmsService {

    @Value("${sms.sailing.url}")
    private  String smsSailingURL;

    @Value("${sms.verify.url}")
    private  String smsVerifyURL;

    @Value("${sms.enable}")
    private  Boolean smsmEnable;

    /**
     * 获取手机验证码
     * @param mobile 手机号码
     * @return
     */
    public RestResponse getSmsCode(String mobile) {
        if (smsmEnable) {
            Map<String,String> map = new HashMap<>();
            map.put("mobile",mobile);
            String s = HttpClientUitl.doPostByJson(smsSailingURL, map);
            JSONObject jsonObject = JSONObject.parseObject(s);
            return RestResponse.success(jsonObject.toJavaObject(Sms.class));
        }
        return RestResponse.success();
    }

    public boolean verifySmsCode(String key,String code) {
        if (smsmEnable) {
            Map<String,String> map = new HashMap<>();
            map.put("verificationKey",key);
            map.put("verificationCode",code);
            String s = HttpClientUitl.doPost(smsVerifyURL,map);
            JSONObject jsonObject = JSONObject.parseObject(s);
            VerifyMessage message = jsonObject.toJavaObject(VerifyMessage.class);
            return message.isResult();
        }
        return true;
    }
}
