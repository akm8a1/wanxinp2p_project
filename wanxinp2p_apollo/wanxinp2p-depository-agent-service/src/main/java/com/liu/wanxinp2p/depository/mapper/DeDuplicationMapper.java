package com.liu.wanxinp2p.depository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.wanxinp2p.depository.entity.DeDuplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实现分布式消息的事物记录
 */
@Mapper
public interface DeDuplicationMapper extends BaseMapper<DeDuplication> {

}
