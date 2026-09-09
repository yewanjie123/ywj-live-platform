package com.ywj.live.api.service;



import com.ywj.live.api.vo.GiftConfigVO;
import com.ywj.live.api.vo.GiftReqVO;

import java.util.List;

public interface IGiftService {

    /**
     * 展示礼物列表
     * @return
     */
    List<GiftConfigVO> listGift();

    /**
     * 送礼
     * @param giftReqVO
     * @return
     */
    boolean send(GiftReqVO giftReqVO);
}