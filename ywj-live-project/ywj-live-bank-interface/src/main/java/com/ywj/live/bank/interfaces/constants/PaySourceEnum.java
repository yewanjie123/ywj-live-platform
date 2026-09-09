package com.ywj.live.bank.interfaces.constants;

/**
 * 支付渠道类型
 */
public enum PaySourceEnum {

    YWJ_LIVING_ROOM(1,"直播间内支付"),
    YWJ_USER_CENTER(2,"用户中心");

    PaySourceEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaySourceEnum find(int code) {
        for (PaySourceEnum value : PaySourceEnum.values()) {
            if(value.getCode() == code) {
                return value;
            }
        }
        return null;
    }

    private int code;
    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}