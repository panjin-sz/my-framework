/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.common;

import java.io.Serializable;

/**
 * 返回对象实体
 *
 * @author panjin
 * @version $Id: OpResponse.java 2016年7月13日 下午2:17:38 $
 */
public class OpResponse implements Serializable {

    /**  */
    private static final long serialVersionUID = 5520118136628542438L;

    /**
     * 成功的code，各系统错误码保持一致
     */
    public static final int SUCCESS_CODE = 1;
    
    /**
     * 应用返回的错误码
     */
    protected int code;
    
    /**
     * 应用返回的（错误）消息
     */
    protected String message;
    
    /**
     * 应用返回的数据
     */
    protected Object data;
    
    /**
     * 某次请求返回的token，用于下一次表单提交
     */
    protected String token;
    
    /**
     * 主要用于前端是H5的场景，用于告诉H5页面，收到请求后是跳下一个页面，还是停留在当前页面
     */
    protected String next;
    
    /**
     * 返回1个表示成功的实例
     * 
     * @return
     */
    public static OpResponse suc() {
        OpResponse resp = new OpResponse();
        resp.code = SUCCESS_CODE;
        return resp;
    }
    
    /**
     * 返回1个表示成功的实例
     * 
     * @param message 要返回的消息
     * @return
     */
    public static OpResponse suc(String message) {
        OpResponse resp = OpResponse.suc();
        resp.message = message;
        return resp;
    }

    /**
     * 返回1个表示失败的实例
     * @param code 错误码
     * @param message 错误信息
     * @return
     */
    public static OpResponse fail(int code, String message) {
        OpResponse resp = new OpResponse();
        resp.code = code;
        resp.message = message;
        return resp;
    }
    
    /**
     * 返回1个表示失败的实例 
     * @param code 错误码
     * @param message 错误信息
     * @param next 下一步，可以使用NextAction枚举类
     * @return
     */
    public static OpResponse fail(int code, String message, String next) {
        OpResponse resp = new OpResponse();
        resp.code = code;
        resp.message = message;
        resp.next = next;
        return resp;
    }
    
    /**
     * 操作结果是否成功
     * 
     * @return
     */
    public boolean isSuc() {
        return code == SUCCESS_CODE;
    }

    /**
     * 操作结果是否失败
     * @return
     */
    public boolean isFail() {
        return !isSuc();
    }
    
    
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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

    public void setNext(String next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return "OpResponse [code=" + code + ", message=" + message + ", data=" + data + ", token=" + token + ", next=" + next + "]";
    }
}
