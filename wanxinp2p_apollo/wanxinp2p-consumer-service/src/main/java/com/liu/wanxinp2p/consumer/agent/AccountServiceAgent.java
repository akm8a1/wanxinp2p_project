package com.liu.wanxinp2p.consumer.agent;

import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountRegisterDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AccountService的远程调用Feign客户端
 * @FeignClient 将对象作为Feign的客户端注入到spring容器中
 */
@FeignClient("account-service")
public interface AccountServiceAgent {

    @PostMapping("/account/l/accounts")
    @Hmily
    RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO);

}
