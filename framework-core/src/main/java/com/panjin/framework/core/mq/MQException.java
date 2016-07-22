/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.mq;

import com.panjin.framework.basic.exception.PlatformException;

/**
 *
 *
 * @author panjin
 * @version $Id: MQException.java 2016年7月22日 下午6:29:35 $
 */
public class MQException extends PlatformException {

    /**  */
    private static final long serialVersionUID = -5315697585669166033L;

    /**
     * 
     * @param code
     *            错误码
     * @param message
     *            错误信息
     */
    public MQException(int code, String message) {
        super(code, message);
    }

    /**
     * 
     * @param message
     *            错误信息
     */
    public MQException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     *            错误信息
     * @param cause
     *            原始异常
     */
    public MQException(String message, Throwable cause) {
        super(message, cause);
    }
}
