package com.ywj.live.im.provider.service;

public interface ImTokenService {
    String createImLoginToken(long userId, int appId);

    Long getUserIdByToken(String token);
}