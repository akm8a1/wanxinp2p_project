package com.liu.wanxinp2p.consumer.agent;

import com.liu.wanxinp2p.api.consumer.model.ConsumerRequest;
import com.liu.wanxinp2p.api.consumer.model.RechargeRequest;
import com.liu.wanxinp2p.api.depository.model.GatewayRequest;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 存管代理服务
 */
@FeignClient("DEPOSITORY-AGENT-SERVICE")
public interface DepositoryServiceAgent {

    @PostMapping("/depository-agent/l/consumers")
    RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest consumerRequest);

    @PostMapping("/depository-agent/l/recharges")
    RestResponse<GatewayRequest> createRechargeRecord(@RequestBody RechargeRequest rechargeRequest);
}
