/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.servlet.json;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import com.panjin.framework.util.JsonUtil;
import com.panjin.framework.web.servlet.HttpServletHolder;
import com.panjin.framework.web.util.WebUtil;

/**
 * web层使用的json工具类
 *
 * @author panjin
 * @version $Id: WebJsonUtil.java 2016年7月25日 下午3:21:03 $
 */
public class WebJsonUtil extends JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebJsonUtil.class);

    /** json响应的contentType */
    public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    /** jsonp返回的方法名其对应的请求参数名 */
    public static final String JSONP_FUNCTION_NAME = "callback";

    /**
     * 根据java对象转换成JSONP字符串
     * 
     * @param object
     *            java对象
     * @return JSONP字符串
     * @see #toJSONPString(String)
     */
    public static final String toJSONPString(Object object) {
        String jsonString = toJSONString(object);
        return toJSONPString(jsonString);
    }

    /**
     * 将JSON字符串转换成JSONP字符串
     * 
     * @param text
     *            JSON字符串
     * @return JSONP字符串
     * @see com.sfpay.framework2.web.servlet.HttpServletHolder
     * @see #toJSONPString(HttpServletRequest, String)
     */
    public static final String toJSONPString(String text) {
        HttpServletRequest request = HttpServletHolder.getCurrentRequest();
        return toJSONPString(request, text);
    }

    /**
     * 将JSON字符串转换成JSONP字符串
     * <p>
     * 当当前HTTP请求中存在{@link #JSONP_FUNCTION_NAME}参数时，转换为JSONP字符串，没有则返回原字符串</br>
     * 必须在接受HTTP请求时使用，这样才能获得当前HTTP请求，并且在非HTTP请求时，也没有转换JSONP字符串的必要</br>
     * JSONP字符串不能再进行反序列化操作
     * 
     * @param request
     *            HTTP请求
     * @param text
     *            JSON字符串
     * @return JSONP字符串
     * @see com.sfpay.framework2.web.servlet.HttpServletHolder
     */
    public static final String toJSONPString(HttpServletRequest request, String text) {
        String functionName = WebUtil.getQueryParameter(request, JSONP_FUNCTION_NAME);
        if (StringUtils.isBlank(functionName)) {
            try {
                request.getParameter(JSONP_FUNCTION_NAME);
            } catch (Exception ignore) {
                logger.warn(ignore.getMessage());
            }
        }

        if (StringUtils.isBlank(functionName)) {
            return text;
        }

        StringBuffer buffer = new StringBuffer(functionName);
        buffer.append('(');
        if (StringUtils.isNotBlank(text)) {
            buffer.append(text);
        }
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * 是否是Json请求
     * 
     * @param request
     *            Http请求
     * @return 是则返回<code>TRUE</code>
     */
    public static final boolean isJsonRequest(HttpServletRequest request, Object handler) {
        // Ajax请求默认认为是json请求
        String requestedWith = request.getHeader("X-Requested-With");
        if (StringUtils.equalsIgnoreCase(requestedWith, "XMLHttpRequest")) {
            return true;
        }

        // 请求url中包含.json或.jsonp的也认为是json请求
        String requestUri = request.getRequestURI();
        if (StringUtils.endsWithIgnoreCase(requestUri, ".json") || StringUtils.endsWithIgnoreCase(requestUri, ".jsonp")) {
            return true;
        }

        // handler如果带了@ResponseBody也认为是JSON请求
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            if (hm.getMethod().getAnnotation(ResponseBody.class) != null) {
                return true;
            }
        }

        return false;
    }

}
