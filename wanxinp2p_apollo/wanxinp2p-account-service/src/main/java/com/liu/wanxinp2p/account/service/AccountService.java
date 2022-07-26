package com.liu.wanxinp2p.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.wanxinp2p.account.entity.Account;
import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountLoginDTO;
import com.liu.wanxinp2p.api.account.model.AccountRegisterDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
/*
统一账号服务Service
 */
public interface AccountService extends IService<Account> {
    /**
     * 获取验证码
     * @param mobile 手机号码
     * @return
     */
    RestResponse getSMSCode(String mobile);

    /**
     * 校验手机号码
     * @param mobile 手机号码
     * @param key 验证码key
     * @param code 验证码
     * @return
     */
    Integer checkMobile(String mobile,String key,String code);

    /**
     * 账户注册
     * @param registerDTO 注册信息
     * @return
     */
    AccountDTO register(AccountRegisterDTO registerDTO);

    /**
     * 登录功能
     * @param accountLoginDTO 封装登录请求数据
     * @return
     */
    AccountDTO login(AccountLoginDTO accountLoginDTO);
}
