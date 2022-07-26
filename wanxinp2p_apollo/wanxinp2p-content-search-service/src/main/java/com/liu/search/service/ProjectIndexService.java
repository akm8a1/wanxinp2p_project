package com.liu.search.service;

import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectQueryParamsDTO;
import com.liu.wanxinp2p.common.domain.PageVO;

/**
 * 标的检索业务层接口
 */
public interface ProjectIndexService {
    PageVO<ProjectDTO> queryProjectIndex(ProjectQueryParamsDTO projectQueryParamsDTO,
                                         Integer pageNo,Integer pageSize,String sortBy,String order);
}
