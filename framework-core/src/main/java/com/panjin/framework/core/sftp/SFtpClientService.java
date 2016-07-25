/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.sftp;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.panjin.framework.basic.log.Log;
import com.panjin.framework.core.sftp.monitor.DownloadMonitor;
import com.panjin.framework.core.sftp.monitor.UploadMonitor;

/**
 *
 *
 * @author panjin
 * @version $Id: SFtpClientService.java 2016年7月25日 下午2:29:47 $
 */
public class SFtpClientService {

    public static final String SLASH = "/";

    private IChannelFactory sftpChannelFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(SFtpClientService.class);

    public SFtpClientService(IChannelFactory sftpChannelFactory) {
        super();
        this.sftpChannelFactory = sftpChannelFactory;
    }

    public void upload(String localDir, String fileName, String remoteDir) {
        ChannelSftp channel = (ChannelSftp) sftpChannelFactory.openChannel();
        if (createCjsDownLoadDirectory(channel, remoteDir)) {
            return;
        }
        try {
            channel.put(localDir + SLASH + fileName, remoteDir + SLASH + fileName, new UploadMonitor());
        } catch (SftpException e) {
            LOGGER.error("upload occurs error.", e);
        } finally {
            sftpChannelFactory.close(channel);
        }

    }

    private boolean createCjsDownLoadDirectory(ChannelSftp channel, String routeDir) {
        try {
            channel.cd(routeDir);
        } catch (SftpException cd) {
            try {
                channel.mkdir(routeDir);
            } catch (SftpException ex) {
                LOGGER.error("the directory " + routeDir + "  create fail.", ex);
                sftpChannelFactory.close(channel);
                return true;// 上传目录不可以创建
            }
        }
        return false;
    }

    public void download(String remoteDir, String fileName, String localDir) {

        ChannelSftp channel = (ChannelSftp) sftpChannelFactory.openChannel();

        if (createCjsDownLoadDirectory(channel, remoteDir)) {
            return;
        }

        // 如果本地目录不存在，就创建
        File local = new File(localDir);
        if (!local.exists()) {
            local.mkdir();
        }

        try {
            // 下载文件到本地
            channel.get(remoteDir + SLASH + fileName, localDir + SLASH + fileName, new DownloadMonitor());
        } catch (SftpException e) {
            LOGGER.error(Log.op("DownloadFileService.cjsDownload()").kv("remoteFile", remoteDir + SLASH + fileName).msg("file down fail").toString(), e);
        } finally {
            sftpChannelFactory.close(channel);
        }
    }

}
