package com.liu.wanxinp2p.consumer.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.wanxinp2p.api.consumer.model.BankCardDTO;
import com.liu.wanxinp2p.consumer.entity.BankCard;
import com.liu.wanxinp2p.consumer.mapper.BankCardMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BankCardServiceImpl extends ServiceImpl<BankCardMapper,BankCard> implements BankCardService {

    /**
     * 根据用户标识获取银行卡信息
     * @param consumerId 用户id
     * @return
     */
    @Override
    public BankCardDTO getByConsumerId(Long consumerId) {
        Wrapper<BankCard> wrapper = new QueryWrapper<BankCard>().eq("CONSUMER_ID",consumerId);
        BankCard bankCard = getOne(wrapper);
        return convertBankCardToDTO(bankCard);
    }

    /**
     * 根据银行卡号获取银行卡信息
     * @param cardNumber 卡号
     * @return
     */
    @Override
    public BankCardDTO getByCardNumber(String cardNumber) {
        Wrapper<BankCard> wrapper = new QueryWrapper<BankCard>().eq("CARD_NUMBER",cardNumber);
        BankCard bankCard = getOne(wrapper);
        return convertBankCardToDTO(bankCard);
    }

    private BankCardDTO convertBankCardToDTO(BankCard entity) {
        if (null == entity) {
            return null;
        }
        BankCardDTO dto = new BankCardDTO();
        BeanUtils.copyProperties(entity,dto);
        return dto;
    }
}
