package com.liu.wanxinp2p.uaa.agent;

import com.liu.wanxinp2p.api.account.model.AccountDTO;
import com.liu.wanxinp2p.api.account.model.AccountLoginDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * AccountService的远程调用Feign客户端
 * @FeignClient 将对象作为Feign的客户端注入到spring容器中
 */
@FeignClient("account-service")
public interface AccountServiceAgent {

    @PostMapping("/account/l/accounts/session")
    RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO) ;

}
