package com.liu.wanxinp2p.depository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.wanxinp2p.depository.entity.DeDuplication;

/**
 * 分布式事物
 */
public interface DeDuplicationService extends IService<DeDuplication> {
    public void addTx(String requestNo);
}
