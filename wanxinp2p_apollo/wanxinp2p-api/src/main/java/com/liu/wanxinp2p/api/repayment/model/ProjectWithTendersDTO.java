package com.liu.wanxinp2p.api.repayment.model;

import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.api.transaction.model.TenderDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 标的还款信息
 */
@Data
public class ProjectWithTendersDTO {
    /**
     * 标的信息
     */
    private ProjectDTO project;
    /**
     * 标的对应的所有投标记录
     */
    private List<TenderDTO> tenders;

    /**
     * 投资人让出利率 ( 投资人让利 )
     */
    private BigDecimal commissionInvestorAnnualRate;

    /**
     * 借款人给平台的利率 ( 借款人让利 )
     */
    private BigDecimal commissionBorrowerAnnualRate;
}
