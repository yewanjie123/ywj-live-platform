package com.ywj.live.bank.interfaces.constants;

public enum PayProductTypeEnum {

    YWJ_COIN(0,"直播间充值-虚拟币产品");

    PayProductTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    Integer code;
    String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}