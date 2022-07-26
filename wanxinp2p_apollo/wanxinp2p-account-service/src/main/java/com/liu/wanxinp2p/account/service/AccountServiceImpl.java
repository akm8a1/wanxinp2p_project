package com.liu.wanxinp2p.account.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.wanxinp2p.account.entity.Account;
import com.liu.wanxinp2p.common.exception.AccountErrorCode;
import com.liu.wanxinp2p.account.mapper.AccountMapper;
import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountLoginDTO;
import com.liu.wanxinp2p.api.account.model.AccountRegisterDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import com.liu.wanxinp2p.common.exception.GlobalException;
import com.liu.wanxinp2p.common.util.PasswordUtil;
import org.dromara.hmily.annotation.Hmily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
统一账号服务Service
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper,Account> implements AccountService{

    public static void main(String[] args) {
        System.out.println(PasswordUtil.generate("admin"));
    }

    public static Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Value("${sms.enable}")
    private  Boolean smsmEnable;

    @Autowired
    private SmsService smsService;

    /**
     * 获取验证码
     * @param mobile 手机号码
     * @return
     */
    @Override
    public RestResponse getSMSCode(String mobile) {
        return smsService.getSmsCode(mobile);
    }

    /**
     * 用户登录，首先校验手机验证码，然后查看数据库中是否这个手机号码对应的用户
     * @param mobile 手机号码
     * @param key 验证码key
     * @param code 验证码
     * @return
     */
    @Override
    public Integer checkMobile(String mobile, String key, String code) {
        boolean verify = smsService.verifySmsCode(key,code);
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile",mobile);
        Integer count = count(wrapper);
        return verify && count>0 ? 1 : 0 ;
    }

    /**
     * 账户注册 : AccountRegisterDTO -> Account -> AccountDTO
     * @param registerDTO 注册信息
     * @return
     */
    @Override
    @Hmily(confirmMethod = "confirmRegister",cancelMethod = "cancelRegister")
    public AccountDTO register(AccountRegisterDTO registerDTO) {
        Account account = new Account();
        //通常username就是mobile，因为前端注册的时候只会输入mobile
        account.setUsername(registerDTO.getUsername());
        account.setMobile(registerDTO.getMobile());
        account.setPassword(PasswordUtil.generate(registerDTO.getPassword()));
        //如果使用验证码注册,那么密码默认为手机号码
        if (smsmEnable) {
            account.setPassword(PasswordUtil.generate(account.getMobile()));
        }
        //c端用户
        account.setDomain("c");
        //在数据库中插入这条数据
        save(account);
        return convertAccountEntityToDTO(account);
    }

    /**
     * 登录
     * @param accountLoginDTO 封装登录请求数据
     * @return
     */
    @Override
    public AccountDTO login(AccountLoginDTO accountLoginDTO) {
        Account account = null;
        //c端用户根据手机号码查询,b端用户根据用户名查询
        if (accountLoginDTO.getDomain().equalsIgnoreCase("c")) {
            account = getAccountByMobile(accountLoginDTO.getMobile());
        } else {
            account = getAccountByUsername(accountLoginDTO.getUsername());
        }
        if (account == null) {
            throw new GlobalException(AccountErrorCode.ERROR_USER_UNREGISTERED);
        }
        AccountDTO accountDTO = convertAccountEntityToDTO(account);
        //如果使用了短信验证码登录,在这之前验证码已经校验成功了，同时已经找到了这个对应的用户，直接返回即可
        if (smsmEnable) {
            return accountDTO;
        }
        //如果没有使用短信验证码，那就用密码来代替验证码进行登录，需要校验密码
        if (PasswordUtil.verify(accountLoginDTO.getPassword(),account.getPassword())) {
            return accountDTO;
        }
        throw new GlobalException(AccountErrorCode.ERROR_USER_INFO);
    }

    /**
     * 根据电话号码mobile获取账户
     * @param mobile
     * @return
     */
    private Account getAccountByMobile(String mobile) {
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("MOBILE",mobile);
        return getOne(wrapper,false);
    }

    private Account getAccountByUsername(String username) {
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("USERNAME",username);
        return getOne(wrapper,false);
    }
    /**
     * 将Account对象转化为AccountDTO对象
     * @param entity
     * @return
     */
    private AccountDTO convertAccountEntityToDTO(Account entity) {
        if (null == entity) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        //通过BeanUtils工具类来复制两个类之间的属性
        BeanUtils.copyProperties(entity,dto);
        return dto;
    }

    public void confirmRegister(AccountRegisterDTO registerDTO) {
        log.info("execute confirRegister");
    }
    public void cancelRegister(AccountRegisterDTO registerDTO) {
        log.info("execute cancelRegister");
        //删除账号
        Wrapper<Account> wrapper = new UpdateWrapper<Account>().eq("MOBILE",registerDTO.getMobile());
    }
}
