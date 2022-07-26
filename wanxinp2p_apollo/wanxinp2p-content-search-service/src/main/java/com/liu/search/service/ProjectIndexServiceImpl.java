package com.liu.search.service;

import com.liu.wanxinp2p.api.transaction.model.ProjectDTO;
import com.liu.wanxinp2p.api.transaction.model.ProjectQueryParamsDTO;
import com.liu.wanxinp2p.common.domain.PageVO;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProjectIndexServiceImpl implements ProjectIndexService{

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //elasticSearch 索引名
    @Value("${wanxinp2p.es.index}")
    private String projectIndex;

    @Override
    public PageVO<ProjectDTO> queryProjectIndex(ProjectQueryParamsDTO projectQueryParamsDTO, Integer pageNo, Integer pageSize, String sortBy, String order) {
        //1.创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(projectIndex);
        //2.搜索条件
        //2.1.创建条件查询对象
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //2.2.非空判断并封装条件
        if (StringUtils.isNotBlank(projectQueryParamsDTO.getName())) {
            queryBuilder.must(QueryBuilders.termQuery("name",projectQueryParamsDTO.getName()));
        }
        if (projectQueryParamsDTO.getStartPeriod() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("period").gte(projectQueryParamsDTO.getStartPeriod()));
        }
        if (projectQueryParamsDTO.getEndPeriod() != null) {
            queryBuilder.must(QueryBuilders.rangeQuery("period").lte(projectQueryParamsDTO.getEndPeriod()));
        }
        //3.创建searchSourceBuilder对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //3.1.封装条件查询对象
        searchSourceBuilder.query(queryBuilder);
        //3.2.设置排序信息
        if (StringUtils.isNotBlank(sortBy) && StringUtils.isNotBlank(order)) {
            if (order.toLowerCase().equals("asc")) {
                searchSourceBuilder.sort(sortBy, SortOrder.ASC);
            }
            if (order.toLowerCase().equals("desc")) {
                searchSourceBuilder.sort(sortBy,SortOrder.DESC);
            }
        } else {
            searchSourceBuilder.sort("createdate",SortOrder.DESC);
        }
        //3.3.设置分页信息
        searchSourceBuilder.from((pageNo-1) * pageSize);
        searchSourceBuilder.size(pageSize);
        //4.封装
        searchRequest.source(searchSourceBuilder);
        List<ProjectDTO> list = new ArrayList<>();
        PageVO<ProjectDTO> pageVO = new PageVO<>();
        try {
            //5.执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //6.获取响应结果
            SearchHits hits = searchResponse.getHits();
            //匹配的总记录数
            long totalHits = hits.getTotalHits().value;
            pageVO.setTotal(totalHits);
            //获取匹配数据
            SearchHit[] searchHits = hits.getHits();
            //7.循环封装DTO
            for (SearchHit hit : searchHits) {
                ProjectDTO projectDTO = new ProjectDTO();
                System.out.println("123");
                Map<String,Object> sourceAsMap = hit.getSourceAsMap();
                Long id = (Long)sourceAsMap.get("id");
                System.out.println(id);
                BigDecimal annualrate = new BigDecimal(String.valueOf(sourceAsMap.get("annualrate")));
                System.out.println(annualrate);
                BigDecimal amount = new BigDecimal(String.valueOf(sourceAsMap.get("amount")));
                System.out.println(amount);
                String projectStatus = (String)sourceAsMap.get("projectstatus");
                System.out.println(projectStatus);
                Integer period = Integer.parseInt(sourceAsMap.get("period").toString());
                System.out.println(period);
                String name = (String)sourceAsMap.get("name");
                System.out.println(name);
                String description = (String)sourceAsMap.get("description");
                System.out.println(description);
                String type = (String)sourceAsMap.get("type");
                System.out.println(type);
                BigDecimal commissionannualrate = new BigDecimal(String.valueOf(sourceAsMap.get("commissionannualrate")));
                System.out.println(commissionannualrate);
                BigDecimal borrowerannualrate = new BigDecimal(String.valueOf(sourceAsMap.get("borrowerannualrate")));
                System.out.println(borrowerannualrate);
                String repaymentway = (String)sourceAsMap.get("repaymentway");
                System.out.println(repaymentway);
                projectDTO.setId(id);
                projectDTO.setAnnualRate(annualrate);
                projectDTO.setAmount(amount);
                projectDTO.setProjectStatus(projectStatus);
                projectDTO.setPeriod(period);
                projectDTO.setName(name);
                projectDTO.setType(type);
                projectDTO.setRepaymentWay(repaymentway);
                projectDTO.setCommissionAnnualRate(commissionannualrate);
                projectDTO.setBorrowerAnnualRate(borrowerannualrate);

                projectDTO.setDescription(description);
                System.out.println(projectDTO);
                list.add(projectDTO);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //8.封装为PageVO对象并返回
        pageVO.setContent(list);
        pageVO.setPageNo(pageNo);
        pageVO.setPageSize(pageSize);
        return pageVO;
    }
}
