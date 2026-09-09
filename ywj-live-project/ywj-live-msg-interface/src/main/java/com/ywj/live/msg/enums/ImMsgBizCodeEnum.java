package com.ywj.live.msg.enums;

public enum ImMsgBizCodeEnum {

    LIVING_ROOM_IM_CHAT_MSG_BIZ(5555,"直播间im聊天消息");
    int code;
    String desc;

    ImMsgBizCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}