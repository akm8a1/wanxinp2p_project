package com.liu.wanxinp2p.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.wanxinp2p.transaction.entity.Tender;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 投标信息表Mapper接口
 */
@Mapper
public interface TenderMapper extends BaseMapper<Tender> {

    @Select("SELECT IFNULL(SUM(AMOUNT), 0.0) FROM tender WHERE PROJECT_ID = #{id} AND STATUS = 1")
    List<BigDecimal> selectAmountInvestedByProjectId(@Param("id") Long id);
}
