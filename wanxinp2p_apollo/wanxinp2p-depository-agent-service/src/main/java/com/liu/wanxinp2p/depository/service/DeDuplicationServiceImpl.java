package com.liu.wanxinp2p.depository.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.wanxinp2p.depository.entity.DeDuplication;
import com.liu.wanxinp2p.depository.mapper.DeDuplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;

@Service
@Slf4j
public class DeDuplicationServiceImpl extends ServiceImpl<DeDuplicationMapper, DeDuplication> implements DeDuplicationService{

    @Override
    public void addTx(String requestNo) {
        DeDuplication deDuplication = new DeDuplication();
        deDuplication.setTxNo(requestNo);
        deDuplication.setCreate_time(Date.valueOf(LocalDate.now()));
        log.info("执行addTx:"+deDuplication);
        save(deDuplication);
    }
}
