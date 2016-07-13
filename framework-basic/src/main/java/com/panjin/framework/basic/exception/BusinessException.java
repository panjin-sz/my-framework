/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.exception;

/**
 *
 *
 * @author panjin
 * @version $Id: BusinessException.java 2016年7月13日 下午2:44:04 $
 */
public class BusinessException extends ApplicationException {

    /**  */
    private static final long serialVersionUID = 151019963631406245L;

    /**
     * 
     */
    public BusinessException() {
        super();
    }

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息
     */
    public BusinessException(int code, String message) {
        super(code, message);
    }

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息
     * @param cause
     *            原始异常
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 
     * @param message
     *            错误信息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     *            错误信息
     * @param cause
     *            原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息
     * @param args
     *            额外参数
     */
    public BusinessException(int code, String message, Object[] args) {
        super(code, message, args);
    }

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息
     * @param cause
     *            原始异常
     * @param args
     *            额外参数
     */
    public BusinessException(int code, String message, Throwable cause, Object[] args) {
        super(code, message, cause, args);
    }

    /**
     * 
     * @param message
     *            错误信息
     * @param args
     *            额外参数
     */
    public BusinessException(String message, Object[] args) {
        super(message, args);
    }

    /**
     * 
     * @param message
     *            错误信息
     * @param args
     *            额外参数
     * @param cause
     *            原始异常
     */
    public BusinessException(String message, Object[] args, Throwable cause) {
        super(message, args, cause);
    }

    /**
     * 为了泛化调用时好判断code
     */
    public String toString() {
        String s = getClass().getName();
        return "{\"class\":\""+s+"\",\"code\":"+this.getCode()+",\"message\":\""+this.getMessage()+"\"}";
    }
}
