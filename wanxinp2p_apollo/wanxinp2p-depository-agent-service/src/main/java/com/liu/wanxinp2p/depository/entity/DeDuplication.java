package com.liu.wanxinp2p.depository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Date;

@Data
@TableName("de_duplication")
public class DeDuplication {

    @TableField("tx_no")
    private String txNo;

    @TableField("create_time")
    private Date create_time;
}
