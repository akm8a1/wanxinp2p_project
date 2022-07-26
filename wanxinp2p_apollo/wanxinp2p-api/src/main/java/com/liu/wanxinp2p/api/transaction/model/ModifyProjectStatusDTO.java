package com.liu.wanxinp2p.api.transaction.model;

import com.alibaba.fastjson.JSON;
import com.liu.wanxinp2p.common.util.EncryptUtil;
import com.liu.wanxinp2p.common.util.RSAUtil;
import lombok.Data;

import java.io.UnsupportedEncodingException;

/**
 * 修改标的状态
 */
@Data
public class ModifyProjectStatusDTO {
    /**
     * 请求流水号
     */
    private String requestNo;
    /**
     * 标的号
     */
    private String projectNo;
    /**
     * 更新标的状态
     */
    private String projectStatus;

    /**
     * 业务实体id
     */
    private Long id;
}
