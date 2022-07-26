package com.liu.wanxinp2p.api.transaction;

import com.liu.wanxinp2p.api.transaction.model.*;
import com.liu.wanxinp2p.common.domain.PageVO;
import com.liu.wanxinp2p.common.domain.RestResponse;

import java.util.List;

/**
 * 交易服务中心API
 */
public interface TransactionApi {

    /**
     * 借款人发标
     * @param projectDTO
     * @return
     */
    RestResponse<ProjectDTO> createProject(ProjectDTO projectDTO);

    /**
     * 检索标的信息
     * @param projectQueryDTO
     * @param order
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO,
                                                   String order,Integer pageNo,
                                                   Integer pageSize,String sortBy);

    /**
     * 管理员审核标的信息
     * @param id
     * @param approveStatus
     * @return
     */
    RestResponse<String> projectsApprovalStatus(Long id,String approveStatus);

    /**
     * 标的信息快速检索
     * @param projectQueryDTO
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param order
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO,Integer pageNo,
                                                   Integer pageSize,String sortBy,String order);

    /**
     * 通过ids获取多个标的
     * @param ids
     * @return
     */
    RestResponse<List<ProjectDTO>> queryProjectsIds(String ids);

    /**
     * 根据标的id查询投标记录
     * @param id
     * @return
     */
    RestResponse<List<TenderOverviewDTO>> queryTendersByProjectId(Long id);

    /**
     * 用户投标
     * @param projectInvestDTO
     * @return
     */
    RestResponse<TenderDTO> createTender(ProjectInvestDTO projectInvestDTO);

    /**
     * 审核标的满标放款
     * @param id
     * @param approveStatus
     * @param commission
     * @return
     */
    RestResponse<String> loansApprovalStatus(Long id,String approveStatus,String commission);


}
