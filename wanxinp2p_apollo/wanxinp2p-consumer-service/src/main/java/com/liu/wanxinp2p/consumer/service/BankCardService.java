package com.liu.wanxinp2p.consumer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.wanxinp2p.api.consumer.model.BankCardDTO;
import com.liu.wanxinp2p.consumer.entity.BankCard;

/**
 * 用户绑定银行卡服务
 */
public interface BankCardService extends IService<BankCard> {

    /**
     * 根据c端用户获取银行卡信息
     * @param consumerId 用户id
     * @return
     */
    BankCardDTO getByConsumerId(Long consumerId);

    /**
     * 根据卡号获取银行卡信息
     * @param cardNumber 卡号
     * @return
     */
    BankCardDTO getByCardNumber(String cardNumber);
}
