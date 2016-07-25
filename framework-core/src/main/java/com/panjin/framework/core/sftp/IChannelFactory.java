/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.sftp;

import com.jcraft.jsch.Channel;

/**
 * use to get Channel
 *
 * @author panjin
 * @version $Id: IChannelFactory.java 2016年7月25日 下午2:26:39 $
 */
public interface IChannelFactory {
    
    /**
     * 
     * @return connected channel;otherwise null if get failure.
     */
    public Channel openChannel();

    /**
     * close channel resources.
     */
    public void close(Channel channel);
    
    /**
     * destory the session
     */
    public void destory();
    
    /**
     * init the sftp params
     */
    public void init();
}
