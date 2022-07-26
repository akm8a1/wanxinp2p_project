package com.liu.wanxinp2p.transaction.service;

import com.liu.wanxinp2p.api.transaction.model.*;
import com.liu.wanxinp2p.common.domain.PageVO;
import com.liu.wanxinp2p.transaction.entity.Project;

import java.util.List;

/**
 * 交易中心Service接口
 */
public interface ProjectService {

    /**
     * 创建标的
     * @param projectDTO
     * @return
     */
    ProjectDTO createProject(ProjectDTO projectDTO);

    /**
     * 根据分页条件检索标的信息
     * @param projectQueryDTO
     * @param order
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    PageVO<ProjectDTO> queryProjectsByQueryDTO(ProjectQueryDTO projectQueryDTO,String order,Integer pageNo,Integer pageSize,String sortBy);

    /**
     * 管理员审核标的信息
     * @param id
     * @param approveStatus
     * @return
     */
    String projectsApprovalStatus(Long id,String approveStatus);

    /**
     * 查询标的
     * @param projectQueryDTO
     * @param order
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    PageVO<ProjectDTO> queryProjects(ProjectQueryDTO projectQueryDTO, String order,
                                     Integer pageNo, Integer pageSize, String sortBy);

    /**
     * 通过ids获取多个标的
     * @param ids
     * @return
     */
    List<ProjectDTO> queryProjectsIds(String ids);

    /**
     * 根据标的id查询投标记录
     * @param id
     * @return
     */
    List<TenderOverviewDTO> queryTendersByProjectId(Long id);

    /**
     * 用户投标
     * @param projectInvestDTO
     * @return
     */
    TenderDTO createTender(ProjectInvestDTO projectInvestDTO);

    /**
     * 审核标的满标放款
     * @param id
     * @param approveStatus
     * @param commission
     * @return
     */
    String loansApprovalStatus(Long id,String approveStatus,String commission);

    /**
     * 执行本地事务
     * @param project
     * @return
     */
    Boolean updateProjectStatusAndStartRepayment(Project project);

}
