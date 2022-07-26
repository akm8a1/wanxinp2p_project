package com.liu.wanxinp2p.repayment.agent;

import com.liu.wanxinp2p.api.depository.model.RepaymentRequest;
import com.liu.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="depository-agent-service")
public interface DepositoryAgentApiAgent {

    @PostMapping("/depository-agent/l/user-auto-pre-transaction")
    RestResponse<String> userAutoPreTransaction(@RequestBody UserAutoPreTransactionRequest userAutoPreTransactionRequest);

    @PostMapping("/depository-agent/l/confirm-repayment")
    RestResponse<String> confirmRepayment(RepaymentRequest repaymentRequest);

}
