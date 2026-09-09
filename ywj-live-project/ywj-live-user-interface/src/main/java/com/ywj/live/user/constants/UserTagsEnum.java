package com.ywj.live.user.constants;

public enum UserTagsEnum {

    IS_RICH((long) Math.pow(2, 0), "是否是有经济实力的用户", "tag_info_01"),
    IS_VIP((long) Math.pow(2, 1), "是否是VIP用户", "tag_info_02"),
    IS_OLD_USER((long) Math.pow(2, 2), "是否是老用户", "tag_info_03");

    long tag;
    String desc;
    String fieldName;

    UserTagsEnum(long tag, String desc, String fieldName) {
        this.tag = tag;
        this.desc = desc;
        this.fieldName = fieldName;
    }

    public long getTag() {
        return tag;
    }

    public String getDesc() {
        return desc;
    }

    public String getFieldName() {
        return fieldName;
    }
}