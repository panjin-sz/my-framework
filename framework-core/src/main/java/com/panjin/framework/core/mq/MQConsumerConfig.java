/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.mq;

/**
 * MQ消费者相关的配置
 *
 * @author panjin
 * @version $Id: MQConsumerConfig.java 2016年7月22日 下午6:38:04 $
 */
public class MQConsumerConfig {

    private int prefetchCount;

    /**
     * @return the prefetchCount
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * @param prefetchCount
     *            the prefetchCount to set
     */
    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }
}
