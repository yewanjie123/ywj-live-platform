package com.ywj.live.living.interfaces.enums;

public enum LivingRoomTypeEnum {

    DEFAULT_LIVING_ROOM(1,"纯绿色直播间"),
    PK_LIVING_ROOM(2,"vip专用直播间");

    LivingRoomTypeEnum(int code, String desc) {
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