package com.liu.wanxinp2p.api.repayment;

import com.liu.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import com.liu.wanxinp2p.common.domain.RestResponse;

/**
 * 还款API
 */
public interface RepaymentApi {

    /**
     * 启动还款
     * @param project
     * @return
     */
    RestResponse<String> startRepayment(ProjectWithTendersDTO project);
}
