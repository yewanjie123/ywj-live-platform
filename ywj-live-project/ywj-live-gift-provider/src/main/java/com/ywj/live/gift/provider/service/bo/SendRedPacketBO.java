package com.ywj.live.gift.provider.service.bo;

import com.ywj.live.gift.dto.RedPacketConfigReqDTO;

public class SendRedPacketBO {
    private Integer price;
    private RedPacketConfigReqDTO reqDTO;

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public RedPacketConfigReqDTO getReqDTO() {
        return reqDTO;
    }

    public void setReqDTO(RedPacketConfigReqDTO reqDTO) {
        this.reqDTO = reqDTO;
    }
}