package com.liu.wanxinp2p.api.search;

import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectQueryParamsDTO;
import com.liu.wanxinp2p.common.domain.PageVO;
import com.liu.wanxinp2p.common.domain.RestResponse;

/**
 * 内容检索服务
 */
public interface ContentSearchApi {

    /**
     * 内容检索服务
     * @param projectQueryParamsDTO
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param order
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjectIndex(ProjectQueryParamsDTO projectQueryParamsDTO,Integer pageNo,Integer pageSize,String sortBy,String order);
}
