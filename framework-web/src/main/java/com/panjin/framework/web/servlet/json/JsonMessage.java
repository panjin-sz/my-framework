/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.servlet.json;

/**
 * Json处理返回信息对象
 * <p>
 * 提供给业务系统使用，同时框架也能感知到业务的处理结果，比如成功或者失败，从而能对json返回进行一定的处理，比如异常处理
 * 
 * @author panjin
 * @version $Id: JsonMessage.java 2016年7月25日 下午4:06:34 $
 */
public class JsonMessage {

    public static final String SUCCESS_CODE = "200";

    public static final String SUCCESS_MESSAGE = "操作成功";

    public static final String SYSTEM_ERROR_CODE = "500";

    public static final String SYSTEM_ERROR_MESSAGE = "系统错误";

    /**
     * 结果码，不能为<code>NULL</code>
     */
    protected String code = SYSTEM_ERROR_CODE;

    /**
     * 结果信息，当处理失败时，不能为<code>NULL</code>
     */
    protected String message = SYSTEM_ERROR_MESSAGE;

    /**
     * 结果对象，可以为<code>NULL</code>
     */
    protected Object data;

    /**
     * 表单防重复的token
     */
    protected String token;

    /**
     * 主要用于前端是H5的场景，用于告诉H5页面，收到请求后是跳下一个页面，还是停留在当前页面
     */
    protected String next;

    public JsonMessage() {

    }

    public JsonMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JsonMessage suc() {
        JsonMessage jsonMsg = new JsonMessage(SUCCESS_CODE, SUCCESS_MESSAGE);
        return jsonMsg;
    }

    public static JsonMessage fail() {
        JsonMessage jsonMsg = new JsonMessage(SYSTEM_ERROR_CODE, SYSTEM_ERROR_MESSAGE);
        return jsonMsg;
    }

    public boolean isSuc() {
        return SUCCESS_CODE.equals(code);
    }

    public String getCode() {
        return code;
    }

    public JsonMessage setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public JsonMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public JsonMessage setData(Object data) {
        this.data = data;
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNext() {
        return next;
    }

    public JsonMessage setNext(String next) {
        this.next = next;
        return this;
    }
}
