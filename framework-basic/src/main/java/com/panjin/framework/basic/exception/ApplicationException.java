/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.exception;

/**
 * 异常基类，带异常编码
 *
 * @author panjin
 * @version $Id: ApplicationException.java 2016年7月13日 下午1:53:44 $
 */
public class ApplicationException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 3860498352600240708L;

    /**
     * 异常错误码
     */
    protected int code;
    
    /**
     * 异常信息的参数
     */
    protected Object[] args;
    
    public ApplicationException() {
        super();
    }
    
    /**
     * 
     * @param message 错误信息
     */
    public ApplicationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 错误信息
     * @param cause 原始异常
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 
     * @param code 错误编码
     * @param message 错误信息
     */
    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 
     * @param code 错误编码
     * @param message 错误信息
     * @param cause 原始异常
     */
    public ApplicationException(int code, String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 
     * @param message 错误信息
     * @param args 额外参数
     */
    public ApplicationException(String message, Object[] args) {
        super(message);
        this.args = args;
    }
    
    /**
     * 
     * @param message 错误信息
     * @param args 额外参数
     * @param cause 原始异常
     */
    public ApplicationException(String message, Object[] args, Throwable cause) {
        super(message, cause);
        this.args = args;
    }
    
    /**
     * 
     * @param code 错误编码
     * @param message 错误信息
     * @param args 额外参数
     */
    public ApplicationException(int code, String message, Object[] args) {
        super(message);
        this.code = code;
        this.args = args;
    }
    
    /**
     * 
     * @param code 错误编码
     * @param message 错误信息
     * @param cause 原始异常
     * @param args 额外参数
     */
    public ApplicationException(int code, String message, Throwable cause, Object[] args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }

    public int getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
    
}
