/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.sftp;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.panjin.framework.basic.exception.BusinessException;
import com.panjin.framework.basic.log.Log;

/**
 *
 *
 * @author panjin
 * @version $Id: SFtpChannelFactory.java 2016年7月25日 下午2:28:07 $
 */
public class SFtpChannelFactory implements IChannelFactory {

    private Session session;

    private AtomicInteger size = new AtomicInteger(0);

    private SFtpConfig sftpConfig;

    public SFtpConfig getSftpConfig() {
        return sftpConfig;
    }

    public void setSftpConfig(SFtpConfig sftpConfig) {
        this.sftpConfig = sftpConfig;
    }

    private static final int MAX_SIZE = 50;
    private static final Logger logger = LoggerFactory.getLogger(SFtpChannelFactory.class);

    public void init() {
        size.set(0);
        JSch jsch = new JSch();
        logger.info(Log.op("SftpChannelFactory.init").msg("init the sftp start.").kv("key", sftpConfig.getPrivatekey()).kv("user", sftpConfig.getUsername())
                .toString());

        try {
            jsch.addIdentity(sftpConfig.getPrivatekey(), sftpConfig.getPrivatekeypassphrase());
            synchronized (this) {
                session = jsch.getSession(sftpConfig.getUsername(), sftpConfig.getHost(), sftpConfig.getPort());
                session.setPassword(sftpConfig.getPassword());
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setTimeout(sftpConfig.getTimeout());
                session.connect();
            }
            logger.info(Log.op("SftpChannelFactory.init").msg("init the sftp successfully.").kv("key", sftpConfig.getPrivatekey())
                    .kv("user", sftpConfig.getUsername()).toString());
        } catch (JSchException e) {
            logger.error(
                    Log.op("SftpChannelFactory.init").msg("init the sftp failure.").kv("key", sftpConfig.getPrivatekey()).kv("user", sftpConfig.getUsername())
                            .toString(), e);
            throw new BusinessException("init sftp failure", e);
        }
    }

    @Override
    public Channel openChannel() {
        if (size.get() > MAX_SIZE) {
            logger.error(Log.op("SftpChannelFactory.openChannel").msg("the channel has arrive at the max number.").kv("size", size.get()).toString());
            throw new BusinessException("the channel has arrive at the max number.");
        }
        logger.info(Log.op("SftpChannelFactory.openChannel").msg("open the channel starts.").kv("key", session.isConnected()).toString());
        try {
            ChannelSftp channelSftp;
            synchronized (this) {
                if (!session.isConnected()) {
                    session.connect();
                }
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
            }
            int count = size.incrementAndGet();
            logger.info(Log.op("SftpChannelFactory.openChannel").msg("open the channel successfully.").kv("key", session.isConnected()).kv("size", count)
                    .toString());
            return channelSftp;
        } catch (JSchException e) {
            logger.error(Log.op("SftpChannelFactory.openChannel").msg("open the channel failure.").toString(), e);
            destory();
            init();
            throw new BusinessException("open the channel failure", e);
        }
    }

    @Override
    public void close(Channel channel) {
        int count = size.decrementAndGet();
        logger.info(Log.op("SftpChannelFactory.close").msg("close the channel.").kv("channel id", channel.getId()).kv("size", count).toString());
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

    public void destory() {
        synchronized (this) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

}
