/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.util;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSON工具类
 *
 * @author panjin
 * @version $Id: JsonUtil.java 2016年7月15日 下午3:01:32 $
 */
public class JsonUtil {

    /**
     * 根据JSON字符串解析出java对象
     * <p>
     * 需要提供java对象类型
     * 
     * @param text
     *            JSON字符串，不能为<code>NULL</code>
     * @param clazz
     *            java对象类型
     * @return java对象
     */
    public static final <T> T parse(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * 根据JSON字符串解析出java对象
     * <p>
     * 必须是JSON字符串中已经包含了java对象类型，或者序列化的信息和类型信息的属性集是一致的
     * 
     * @param text
     *            JSON字符串
     * @return java对象
     */
    @SuppressWarnings("unchecked")
    public static final <T> T parse(String text) {
        return (T) JSON.parse(text);
    }

    /**
     * 根据字节数组解析出java对象
     * <p>
     * 需要提供java对象类型
     * 
     * @param input
     *            字节数组
     * @param clazz
     *            java对象类型
     * @return java对象
     */
    public static final <T> T parse(byte[] input, Class<T> clazz) {
        return JSON.parseObject(input, clazz, new Feature[0]);
    }

    /**
     * 根据JSON字节数组解析出java对象
     * <p>
     * 必须是JSON字节数组中已经包含了java对象类型，或者序列化的信息和类型信息的属性集是一致的
     * 
     * @param input
     *            字节数组
     * @return java对象
     */
    @SuppressWarnings("unchecked")
    public static final <T> T parse(byte[] input) {
        return (T) JSON.parse(input, new Feature[0]);
    }

    /**
     * 根据JSON字符串解析出list
     * 
     * @param text
     *            JSON 字符串
     * @param clazz
     *            java对象类型
     * @return list对象
     */
    public static final <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /**
     * 根据JSON字节数组解析出list
     * 
     * @param input
     *            字节数组
     * @param clazz
     *            java对象类型
     * @return list对象
     */
    public static final <T> List<T> parseArray(byte[] input, Class<T> clazz) {
        try {
            return JSON.parseArray(new String(input, "UTF-8"), clazz);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据java对象转换成JSON字符串
     * <p>
     * 按照默认方式转换，只输出属性和属性值信息，不加入任何其他类型信息
     * 
     * @param object
     *            java对象
     * @return JSON字符串
     */
    public static final String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    /**
     * 根据java对象和指定的时间格式化将java对象转换成JSON字符串
     * <p>
     * java对象中的时间属性将被格式化为指定的时间格式
     * 
     * @param object
     *            java对象
     * @param dateFormat
     *            指定的时间格式化字符串，不能为<code>NULL</code>
     * @return JSON字符串
     */
    public static final String toJSONString(Object object, String dateFormat) {
        return JSON.toJSONStringWithDateFormat(object, dateFormat, new SerializerFeature[0]);
    }

    /**
     * 根据java对象转换成字节数组
     * <p>
     * 按照默认方式转换，只输出属性和属性值信息，不加入任何其他类型信息
     * 
     * @param object
     *            java对象
     * @return 字节数组
     */
    public static final byte[] toJSONBytes(Object object) {
        return JSON.toJSONBytes(object, new SerializerFeature[0]);
    }

}
