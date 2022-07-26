package com.liu.wanxinp2p.transaction.agent;

import com.liu.wanxinp2p.api.consumer.model.BalanceDetailsDTO;
import com.liu.wanxinp2p.api.consumer.model.ConsumerDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Consumer-Service服务代理
 */
@FeignClient("consumer-service")
public interface ConsumerServiceAgent {

    @GetMapping("/consumer/l/currentConsumer/{mobile}")
    RestResponse<ConsumerDTO> getCurrentConsumer(@PathVariable String mobile);

    @GetMapping("/consumer/l/balances/{userNo}")
    RestResponse<BalanceDetailsDTO> getBalance(@PathVariable String userNo);
}
