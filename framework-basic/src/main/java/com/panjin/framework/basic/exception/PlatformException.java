/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.exception;

/**
 *
 *
 * @author panjin
 * @version $Id: PlatformException.java 2016年7月22日 下午6:28:05 $
 */
public class PlatformException extends ApplicationException {

    /**  */
    private static final long serialVersionUID = -7936747477834226653L;

    /**
     * 
     */
    public PlatformException() {
        super();
    }

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息
     */
    public PlatformException(int code, String message) {
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
    public PlatformException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 
     * @param message
     *            错误信息
     */
    public PlatformException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     *            错误信息
     * @param cause
     *            原始异常
     */
    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息 原始异常
     * @param args
     *            额外参数
     */
    public PlatformException(int code, String message, Object[] args) {
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
    public PlatformException(int code, String message, Throwable cause, Object[] args) {
        super(code, message, cause, args);
    }

    /**
     * 
     * @param message
     *            错误信息
     * @param args
     *            额外参数
     */
    public PlatformException(String message, Object[] args) {
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
    public PlatformException(String message, Object[] args, Throwable cause) {
        super(message, args, cause);
    }
}
