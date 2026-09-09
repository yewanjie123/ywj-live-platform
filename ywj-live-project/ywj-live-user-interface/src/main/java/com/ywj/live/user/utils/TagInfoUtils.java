package com.ywj.live.user.utils;
public class TagInfoUtils {

    /**
     * 判断是否存在某个标签
     *
     * @param tagInfo 用户当前的标签值
     * @param matchTag 被查询是否匹配的标签值
     * @return
     */
    public static boolean isContain(Long tagInfo, Long matchTag) {
        //需要根据标签枚举中的fieldName来识别需要匹配MySQL表中哪个字段的标签值
        return tagInfo != null && matchTag != null && matchTag > 0 && (tagInfo & matchTag) == matchTag;
    }

}