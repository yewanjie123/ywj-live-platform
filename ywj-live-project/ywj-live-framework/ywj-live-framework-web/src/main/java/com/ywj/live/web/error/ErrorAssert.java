package com.ywj.live.web.error;

public class ErrorAssert {
    /**
     * 判断参数不能为空
     * @param obj
     * @param baseError
     */
    public static void isNotNull(Object obj, BaseError baseError) {
        if (obj == null) {
            throw new BaseErrorException(baseError);
        }
    }

    /**
     * 判断字符串不能为空
     * @param str
     * @param baseError
     */
    public static void isNotBlank(String str, BaseError baseError) {
        if (str == null || str.trim().length() == 0) {
            throw new BaseErrorException(baseError);
        }
    }

    /**
     * flag == true
     * @param flag
     * @param baseError
     */
    public static void isTure(boolean flag, BaseError baseError) {
        if (!flag) {
            throw new BaseErrorException(baseError);
        }
    }

    /**
     * flag == true
     * @param flag
     * @param exception
     */
    public static void isTure(boolean flag, BaseErrorException exception) {
        if (!flag) {
            throw exception;
        }
    }
}