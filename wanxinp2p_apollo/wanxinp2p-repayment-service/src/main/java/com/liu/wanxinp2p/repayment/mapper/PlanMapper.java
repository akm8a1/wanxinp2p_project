package com.liu.wanxinp2p.repayment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.wanxinp2p.repayment.entity.RepaymentPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 借款人还款计划 Mapper接口
 */
@Mapper
public interface PlanMapper extends BaseMapper<RepaymentPlan> {

    /**
     * 查询所有到期还款计划
     * @param date 日期
     * @return
     */
    @Select("SELECT * FROM repayment_plan WHERE DATE_FORMAT(SHOULD_REPAYMENT_DATE,'%Y-%m-%d') = #{date} AND REPAYMENT_STATUS='0'")
    List<RepaymentPlan> selectDueRepayment(String date);

    /**
     * 查询所有到期的还款计划
     * @param date
     * @param shardingCount
     * @param shardingItem
     * @return
     */
    @Select("SELECT * FROM repayment_plan WHERE DATE_FORMAT(SHOULD_REPAYMENT_DATE,'%Y-%m-%d') = #{date} AND REPAYMENT_STATUS = '0' AND MOD(number_of_periods,# {shardingCount}) = #{shardingItem}")
    List<RepaymentPlan> selectDueRepayment(@Param("date")String date,
                                           @Param("shardingCount")int shardingCount,
                                           @Param("shardingItem")int shardingItem);
}
