package com.liu.search.controller;

import com.liu.search.service.ProjectIndexService;
import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectQueryParamsDTO;
import com.liu.wanxinp2p.common.domain.PageVO;
import com.liu.wanxinp2p.common.domain.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(value="检索服务",tags = "ContentSearch", description = "检索服务API")
public class ContentSearchController {

    @Autowired
    private ProjectIndexService projectIndexService;

    @ApiOperation("Elastic-Search搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectQueryParamsDTO",value = "查询信息对象", required = true, dataType = "ProjectQueryParamsDTO",paramType = "body"),
            @ApiImplicitParam(name="pageNo", value = "页号", required = true, dataType = "Integer",paramType = "query"),
            @ApiImplicitParam(name="pageSize", value = "页大小", required = true, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name="sortBy", value = "排序列", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name="order", value="排序方式", required = false, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/l/projects/indexes/q")
    public RestResponse<PageVO<ProjectDTO>> queryProjectIndex(
            @RequestBody ProjectQueryParamsDTO projectQueryParamsDTO,
            @RequestParam Integer pageNo,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {
        System.out.println(projectQueryParamsDTO);
        PageVO<ProjectDTO> projects = projectIndexService.queryProjectIndex(projectQueryParamsDTO,pageNo,pageSize,sortBy ,order);
        return RestResponse.success(projects);
    }

    @PostMapping("/test")
    public String test(@RequestBody ProjectQueryParamsDTO projectQueryParamsDTO) {
        System.out.println(projectQueryParamsDTO);
        return "good";
    }
}
