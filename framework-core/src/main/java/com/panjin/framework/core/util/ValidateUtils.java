/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.util;

import java.util.List;

/**
 *
 *
 * @author panjin
 * @version $Id: ValidateUtils.java 2016年7月18日 下午4:43:40 $
 */
public final class ValidateUtils {

    public static final String NULL_STRING = "null";

    public static final String NULL_STRING_UPPER = "NULL";

    /**
     * 保证字符串不能为null或者空字符串
     * 
     * @param paramName
     *            参数描述
     * @param param
     *            参数名
     */
    public static void ensureParamNotNullEmpty(String paramName, String param) {
        if (!isNotNullEmpty(param)) {
            throw new IllegalArgumentException("param " + paramName + " can not be null or empty!");
        }
    }

    /**
     * 保证字符串不能为null或者空字符串
     * 
     * @param param
     *            参数值
     * @return 判断结果
     */
    public static boolean isNotNullEmpty(String param) {
        if (param != null && !param.trim().isEmpty() && !param.equalsIgnoreCase(NULL_STRING)) {
            return true;
        }
        return false;
    }

    /**
     * 保证字符串集合不能为null或者空字符串
     * 
     * @param params
     *            参数值
     * @return 判断结果
     */
    public static boolean isNotNullEmpty(String... params) {
        boolean ret = true;
        for (String param : params) {
            ret = ret && isNotNullEmpty(param);
        }
        return ret;
    }

    /**
     * 保证对象不为null
     * 
     * @param param
     *            要判断是否为null的对象
     * @return 判断结果
     */
    public static boolean isNotNull(Object param) {
        if (param != null) {
            return true;
        }
        return false;
    }

    /**
     * 保证对象集合不为null
     * 
     * @param objects
     *            要判断是否为null的对象
     * @return 判断结果
     */
    public static boolean isNotNull(Object... objects) {
        boolean ret = true;
        for (Object obj : objects) {
            ret = ret && isNotNull(obj);
        }
        return ret;
    }

    /**
     * 保证List不能为null或者空，若为空，则抛出IllegalArgumentException
     * 
     * @param paramName
     *            参数描述
     * @param list
     *            要判断是否为null或者空的列表
     */
    public static void ensureParamNotNullEmpty(String paramName, List<?> list) {
        if (!isNotNullEmpty(list)) {
            throw new IllegalArgumentException("param " + paramName + " can not be null or empty!");
        }
    }

    /**
     * 保证List不能为null或者空
     * 
     * @param list
     *            要判断是否为null或者空的列表
     * @return 判断结果
     */
    public static boolean isNotNullEmpty(List<?> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 保证字符串不能为空字符串
     * 
     * @param paramName
     *            参数描述
     * @param param
     *            参数值
     */
    public static void ensureParamNotEmpty(String paramName, String param) {
        if (param != null && param.trim().isEmpty()) {
            throw new IllegalArgumentException("param " + paramName + " can not be empty!");
        }
    }

    /**
     * 保证参数不为null
     * 
     * @param paramName
     *            参数描述
     * @param param
     *            参数值
     */
    public static void ensureParamNotNull(String paramName, Object param) {
        if (param == null) {
            throw new IllegalArgumentException("param " + paramName + " can not be null!");
        }
    }

    /**
     * 检查一些值是否在提供的列表中
     * 
     * @param paramName
     *            参数描述
     * @param value
     *            参数值
     * @param list
     *            用户提供的列表
     */
    public static void ensureIntEnumParam(String paramName, int value, List<Integer> list) {
        if (!list.contains(value)) {
            throw new IllegalArgumentException(paramName + " is " + value + ",should be in list " + list);
        }
    }

    /**
     * 参数为null或者空
     * 
     * @param param
     * @return
     */
    public static boolean isNullOrEmpty(String param) {
        return !isNotNullEmpty(param);
    }

    /**
     * 参数是否为null
     * 
     * @param param
     * @return
     */
    public static boolean isNull(Object param) {
        return !isNotNull(param);
    }

    /**
     * 判断String是否为null
     * 
     * @param param
     * @param checkNullString
     * @return
     */
    public static boolean isStringNotNull(String param) {
        if (param != null && !NULL_STRING.equals(param) && !NULL_STRING_UPPER.equals(param)) {
            return true;
        }
        return false;
    }

    /**
     * 是否不为null但为空
     * 
     * @param param
     * @return
     */
    public static boolean isNotNullButEmpty(String param) {
        boolean ret = false;
        if (param != null && param.trim().isEmpty()) {
            ret = true;
        }
        return ret;
    }

}
