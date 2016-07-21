/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.mq;

/**
 * MQ生产者相关的配置
 *
 * @author panjin
 * @version $Id: MQProducerConfig.java 2016年7月20日 上午11:27:44 $
 */
public class MQProducerConfig {

    /** 消息是否需要confirm */
    private boolean requireConfirm;

    /** 消息confirm的超时时间(单位:秒) */
    private long confirmTimeout;

    /**
     * @return the requireConfirm
     */
    public boolean isRequireConfirm() {
        return requireConfirm;
    }

    /**
     * @param requireConfirm
     *            the requireConfirm to set
     */
    public void setRequireConfirm(boolean requireConfirm) {
        this.requireConfirm = requireConfirm;
    }

    /**
     * @return the confirmTimeout
     */
    public long getConfirmTimeout() {
        return confirmTimeout;
    }

    /**
     * @param confirmTimeout
     *            the confirmTimeout to set
     */
    public void setConfirmTimeout(long confirmTimeout) {
        this.confirmTimeout = confirmTimeout;
    }

}
