/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.mq;

import java.io.Serializable;
import java.util.Map;

/**
 *
 *
 * @author panjin
 * @version $Id: MQTransInfo.java 2016年7月22日 下午6:33:20 $
 */
public class MQTransInfo implements Serializable {

    /**  */
    private static final long serialVersionUID = -1837561634889300516L;

    private String message;

    private Map<String, String> attachments;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
}
