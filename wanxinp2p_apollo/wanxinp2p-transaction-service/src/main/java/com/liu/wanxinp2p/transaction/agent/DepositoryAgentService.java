package com.liu.wanxinp2p.transaction.agent;

import com.liu.wanxinp2p.api.depository.model.LoanRequest;
import com.liu.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import com.liu.wanxinp2p.api.transaction.model.ModifyProjectStatusDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("depository-agent-service")
public interface DepositoryAgentService {

    @PostMapping(value = "/depository-agent/l/createProject")
    RestResponse<String> createProject(@RequestBody ProjectDTO projectDTO);

    @PostMapping("/depository-agent/l/user-auto-pre-transaction")
    RestResponse<String> userAutoPreTransaction(UserAutoPreTransactionRequest userAutoPreTransactionRequest);

    @PostMapping("/depository-agent/l/confirm-loan")
    RestResponse<String> confirmLoan(LoanRequest loanRequest);

    @PostMapping("/depository-agent/l/modify-project-status")
    RestResponse<String> modifyProjectStatus(ModifyProjectStatusDTO
                                                     modifyProjectStatusDTO);
}
