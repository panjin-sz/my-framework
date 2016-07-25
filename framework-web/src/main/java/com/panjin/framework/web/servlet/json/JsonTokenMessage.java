/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.servlet.json;

/**
 * 带有令牌的Json处理返回信息对象
 *
 * @author panjin
 * @version $Id: JsonTokenMessage.java 2016年7月25日 下午5:41:21 $
 */
public class JsonTokenMessage extends JsonMessage {
    
    private String formToken;

    public JsonTokenMessage(JsonMessage jsonMessage) {
        setCode(jsonMessage.getCode()).setMessage(jsonMessage.getMessage())
                .setData(jsonMessage.getData());
    }

    public String getFormToken() {
        return formToken;
    }

    public JsonTokenMessage setFormToken(String formToken) {
        this.formToken = formToken;
        return this;
    }
}
