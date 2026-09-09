package com.ywj.live.api.vo;

import com.ywj.live.gift.dto.ShopCarItemRespDTO;

import java.util.List;

public class ShopCarRespVO {

    private Long userId;
    private Integer roomId;
    private List<ShopCarItemRespDTO> shopCarItemRespDTOS;
    private Integer totalPrice;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<ShopCarItemRespDTO> getShopCarItemRespDTOS() {
        return shopCarItemRespDTOS;
    }

    public void setShopCarItemRespDTOS(List<ShopCarItemRespDTO> shopCarItemRespDTOS) {
        this.shopCarItemRespDTOS = shopCarItemRespDTOS;
    }
}