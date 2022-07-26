package com.liu.wanxinp2p.uaa.service;


import com.liu.wanxinp2p.uaa.domain.OauthClientDetails;
import com.liu.wanxinp2p.uaa.domain.OauthClientDetailsDto;

import java.util.List;

public interface OauthService {

    OauthClientDetails loadOauthClientDetails(String clientId);

    List<OauthClientDetailsDto> loadAllOauthClientDetailsDtos();

    void archiveOauthClientDetails(String clientId);

    OauthClientDetailsDto loadOauthClientDetailsDto(String clientId);

    void registerClientDetails(OauthClientDetailsDto formDto);
    
}