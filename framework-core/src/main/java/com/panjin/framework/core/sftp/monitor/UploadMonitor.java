/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.sftp.monitor;

import java.util.concurrent.atomic.AtomicInteger;

import com.jcraft.jsch.SftpProgressMonitor;

/**
 *
 *
 * @author panjin
 * @version $Id: UploadMonitor.java 2016年7月25日 下午2:30:47 $
 */
public class UploadMonitor implements SftpProgressMonitor {
    
    private final AtomicInteger transferred = new AtomicInteger();

    /** 
     * @see com.jcraft.jsch.SftpProgressMonitor#init(int, java.lang.String, java.lang.String, long)
     */
    @Override
    public void init(int op, String src, String dest, long max) {
        // TODO Auto-generated method stub

    }

    /** 
     * @see com.jcraft.jsch.SftpProgressMonitor#count(long)
     */
    @Override
    public boolean count(long count) {
        transferred.incrementAndGet();
        return true;
    }

    /** 
     * @see com.jcraft.jsch.SftpProgressMonitor#end()
     */
    @Override
    public void end() {
        // TODO Auto-generated method stub

    }

}
