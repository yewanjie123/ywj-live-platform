package com.ywj.live.gift.interfaces;


import com.ywj.live.gift.dto.GiftRecordDTO;

public interface IGiftRecordRpc {

    /**
     * 插入单个礼物信息
     *
     * @param giftRecordDTO
     */
    void insertOne(GiftRecordDTO giftRecordDTO);

}