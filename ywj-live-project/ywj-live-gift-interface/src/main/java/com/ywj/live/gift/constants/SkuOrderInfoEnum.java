package com.ywj.live.gift.constants;

/**
 * 商品订单信息枚举
 */
public enum SkuOrderInfoEnum {

    PREPARE_PAY(0,"待支付状态"),
    HAS_PAY(1,"已支付状态"),
    END(2,"订单已关闭");

    Integer code;
    String desc;

    SkuOrderInfoEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}