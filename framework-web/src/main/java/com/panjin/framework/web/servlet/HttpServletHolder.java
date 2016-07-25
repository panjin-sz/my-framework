/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP SERVLET请求和响应持有者
 * <p>
 * 持有请求和响应的本地线程变量，请在请求进入后设置变量，在返回前清除变量
 * </p>
 * 
 * @author panjin
 * @version $Id: HttpServletHolder.java 2016年7月25日 下午3:23:31 $
 */
public class HttpServletHolder {

    public static final String UNIQUE_ID_HEADER_NAME = "ihome-unique-id";

    /** 请求的本地线程变量 */
    private static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<HttpServletRequest>();

    /** 响应的本地线程变量 */
    private static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<HttpServletResponse>();

    /** 标识本次请求的唯一id */
    private static ThreadLocal<String> inheritableUniqueId = new InheritableThreadLocal<String>();

    /**
     * 设置请求和响应的本地线程变量
     * 
     * @param request
     *            HTTP SERVLET请求
     * @param response
     *            HTTP SERVLET响应
     */
    public static void set(HttpServletRequest request, HttpServletResponse response) {
        currentRequest.set(request);
        currentResponse.set(response);
    }

    public static void set(HttpServletRequest request, HttpServletResponse response, String uniqueId) {
        currentRequest.set(request);
        currentResponse.set(response);
        setUid(uniqueId);
    }

    /**
     * 设置标识请求的唯一id
     * 
     * @param uniqueId
     *            唯一id
     */
    public static void setUid(String uniqueId) {
        inheritableUniqueId.set(uniqueId);
    }

    /**
     * 获取当前HTTP SERVLET请求
     * 
     * @return 当前HTTP SERVLET响应
     */
    public static HttpServletRequest getCurrentRequest() {
        return currentRequest.get();
    }

    /**
     * 获取当前HTTP SERVLET响应
     * 
     * @return 当前HTTP SERVLET响应
     */
    public static HttpServletResponse getCurrentResponse() {
        return currentResponse.get();
    }

    /**
     * 获取标识当前请求的唯一id
     * 
     * @return
     */
    public static String getUid() {
        return inheritableUniqueId.get();
    }

    /**
     * 清除请求和响应的本地线程变量
     */
    public static void remove() {
        currentRequest.remove();
        currentResponse.remove();
        inheritableUniqueId.remove();
    }
}
