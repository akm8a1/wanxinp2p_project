package com.liu.wanxinp2p.repayment.agent;

import com.liu.wanxinp2p.api.consumer.model.BorrowDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "consumer-service")
public interface ConsumerApiAgent {

    @GetMapping(value = "/consumer/l/borrowers/{id}")
    RestResponse<BorrowDTO> getBorrowerMobile(@PathVariable("id")Long id);

}
