package com.liu.wanxinp2p.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sms {
    private Integer code;
    private String msg;
    private Map<String,String> result;
}
