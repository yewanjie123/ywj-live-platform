package com.ywj.live.gift.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class GiftRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8953750410198506471L;

    private Long id;
    private Long userId;
    private Long objectId;
    private Integer source;
    private Integer price;
    private Integer priceUnit;
    private Integer giftId;
    private Date sendTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(Integer priceUnit) {
        this.priceUnit = priceUnit;
    }

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "GiftRecordPO{" +
                "id=" + id +
                ", userId=" + userId +
                ", objectId=" + objectId +
                ", source=" + source +
                ", price=" + price +
                ", priceUnit=" + priceUnit +
                ", giftId=" + giftId +
                ", sendTime=" + sendTime +
                '}';
    }
}