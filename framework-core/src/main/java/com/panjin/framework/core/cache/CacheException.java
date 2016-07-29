/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.cache;

/**
 * 缓存异常
 *
 * @author panjin
 * @version $Id: CacheException.java 2016年7月29日 上午10:15:23 $
 */
public class CacheException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 994170161352418023L;

    /**
     * 
     */
    public CacheException() {
        super();
    }

    /**
     * @param message
     *            错误信息
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * @param message
     *            错误信息
     * @param cause
     *            异常堆栈
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     *            异常堆栈
     */
    public CacheException(Throwable cause) {
        super(cause);
    }
}
