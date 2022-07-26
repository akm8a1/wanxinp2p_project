package com.liu.wanxinp2p.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyMessage {
    private int code;
    private String msg;
    private boolean result;
}
