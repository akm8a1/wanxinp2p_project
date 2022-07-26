package com.liu.wanxinp2p.api.account;

import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountLoginDTO;
import com.liu.wanxinp2p.api.account.model.AccountRegisterDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;

/**
 * 账户服务API
 */
public interface AccountAPI {
    /**
     * 获取短信验证码
     * @param mobile 手机号码
     * @return
     */
    RestResponse getSMSCode(String mobile);

    /**
     * 校验验证码
     * @param mobile 手机号码
     * @param key key
     * @param code 验证码
     * @return
     */
    RestResponse<Integer> checkMobile(String mobile, String key, String code);

    /**
     * 用户注册 ： 保存用户登录账号
     * @param accountRegisterDTO 前端传递的用户注册对象
     * @return 传递给前端展示的用户账号信息 AccountDTO
     */
    RestResponse<AccountDTO> register(AccountRegisterDTO accountRegisterDTO);

    /**
     * 用户登录
     * @param accountLoginDTO 用户登录信息
     * @return
     */
    RestResponse<AccountDTO> login(AccountLoginDTO accountLoginDTO);
}
