/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.exception;

import com.panjin.framework.basic.common.IErrors;
import com.panjin.framework.basic.common.OpResponse;
import com.panjin.framework.basic.exception.ApplicationException;
import com.panjin.framework.basic.exception.PlatformException;

/**
 *
 *
 * @author panjin
 * @version $Id: WebErrors.java 2016年7月25日 下午5:33:32 $
 */
public enum WebErrors implements IErrors<OpResponse> {
    TOKEN_EMPTY(600, "token不允许为空"), TOKEN_NOT_EXIST(601, "token已过期或为无效token");

    private int code;

    private String message;

    private WebErrors(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public OpResponse parse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OpResponse parse(Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OpResponse parseMsg(String message, Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationException exp() {
        return new PlatformException(code, message);
    }

    @Override
    public ApplicationException exp(Object... args) {
        String formatReason = String.format(message, args);
        return new PlatformException(code, formatReason, args);
    }

    @Override
    public ApplicationException exp(Throwable cause, Object... args) {
        String formatReason = String.format(message, args);
        return new PlatformException(code, formatReason, cause, args);
    }

    @Override
    public ApplicationException expMsg(String message, Object... args) {
        String formatReason = String.format(message, args);
        return new PlatformException(formatReason, args);
    }

    @Override
    public ApplicationException expMsg(String message, Throwable cause, Object... args) {
        String formatReason = String.format(message, args);
        return new PlatformException(code, formatReason, cause, args);
    }
}
