package com.ywj.live.account.provider.service;

import org.springframework.stereotype.Service;

@Service
public interface IAccountTokenService {
    String createAndSaveLoginToken(Long userId);

    Long getUserIdByToken(String tokenKey);
}