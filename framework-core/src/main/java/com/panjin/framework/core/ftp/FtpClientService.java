/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panjin.framework.basic.log.Log;

/**
 *
 *
 * @author panjin
 * @version $Id: FtpClientService.java 2016年7月25日 下午2:18:10 $
 */
public class FtpClientService {

    private static final Logger logger = LoggerFactory.getLogger(FtpClientService.class);

    private FtpConfig ftpconfig;

    public FtpClientService(FtpConfig ftpconfig) {
        this.ftpconfig = ftpconfig;
    }

    public FTPClient getFtpClient() {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(ftpconfig.getControlencoding());
        try {
            logger.info(Log.op("getFtpClient").msg("ftp connect starts.").toString());
            ftpClient.connect(ftpconfig.getHost(), ftpconfig.getPort());
            logger.info(Log.op("getFtpClient").msg("ftp connect successes.").toString());
        } catch (IOException e) {
            logger.error(Log.op("getFtpClient").msg("ftp connect failure.").kv("host", ftpconfig.getHost()).kv("message", e.getMessage()).toString(), e);
            FtpErrorCode.FTP_CONNECT_FAILURE.exp();
        }
        try {
            logger.info(Log.op("getFtpClient").msg("ftp login starts.").toString());
            ftpClient.login(ftpconfig.getUsername(), ftpconfig.getPassword());
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setConnectTimeout(1000 * 60);
            ftpClient.setDefaultTimeout(1000 * 60);
            ftpClient.setDataTimeout(1000 * 60);
            ftpClient.setBufferSize(1024 * 2);
            ftpClient.enterRemotePassiveMode();
            ftpClient.enterLocalPassiveMode();
            logger.info(Log.op("getFtpClient").msg("ftp login successes.").toString());
        } catch (IOException e) {
            logger.error(
                    Log.op("getFtpClient").msg("ftp login failure.").kv("user", ftpconfig.getUsername()).kv("password", ftpconfig.getPassword())
                            .kv("message", e.getMessage()).toString(), e);
            FtpErrorCode.FTP_LOGIN_FAILURE.exp();
        }

        return ftpClient;
    }

    /**
     * download directory from remote directory.
     * 
     * @param remoteDirectory
     *            remote directory
     * @param fileName
     *            file name regex
     * @param localDirectory
     *            local directory
     */
    public void download(String remoteDirectory, final String fileName, String localDirectory) {
        FTPClient ftpClient = getFtpClient();
        try {
            ftpClient.changeWorkingDirectory(remoteDirectory);
        } catch (IOException e) {
            destroy(ftpClient);
            logger.error(Log.op("download").msg("change the directory failure").kv("directory", remoteDirectory).toString(), e);
            FtpErrorCode.CHANGE_DIRECTORY_FAILURE.exp(remoteDirectory);
        }
        FTPFile[] files = null;
        try {
            files = ftpClient.listFiles(ftpClient.printWorkingDirectory(), new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile file) {
                    if (Pattern.matches(fileName, file.getName())) {
                        return true;
                    }
                    return false;
                }
            });
        } catch (IOException e) {
            destroy(ftpClient);
            logger.error(Log.op("download").msg("list the file failue").kv("directory", remoteDirectory).toString(), e);
            FtpErrorCode.FTP_DOWNLOAD_FAILURE.exp(fileName);
        }

        // 如果本地目录不存在，就创建
        File localDir = new File(localDirectory);
        if (!localDir.exists()) {
            localDir.mkdir();
        }

        Map<String, List<FTPFile>> ftpFileMap = group(files);
        List<FTPFile> ftpFileList = lastVersionFiles(ftpFileMap);
        List<String> fileNameList = new ArrayList<String>();
        for (FTPFile file : ftpFileList) {
            try (FileOutputStream fos = new FileOutputStream(localDirectory + File.separator + file.getName());
                    BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                logger.info(Log.op("download").msg("download the file starts.").kv("fileName", file.getName()).toString());
                boolean result = ftpClient.retrieveFile(file.getName(), bos);
                bos.flush();
                fos.flush();
                logger.info(Log.op("download").msg("download the file finished.").kv("fileName", file.getName()).kv("result", result).toString());
                // set the modify time consist with ftp server.
                new File(localDirectory + File.separator + file.getName()).setLastModified(file.getTimestamp().getTime().getTime());
                fileNameList.add(file.getName());
            } catch (IOException e) {
                logger.error(Log.op("download").msg("list the file failue").kv("fileName", file.getName()).toString(), e);
                // FtpErrorCode.FTP_DOWNLOAD_FAILURE.exp(file.getName());
            }
        }
        destroy(ftpClient);
    }

    // 通过文件名分组，同一组包含不同的版本的文件
    private Map<String, List<FTPFile>> group(FTPFile[] files) {
        Pattern pattern = Pattern.compile("_\\d{2}\\.csv$");
        Map<String, List<FTPFile>> groupFileMap = new HashMap<String, List<FTPFile>>();
        for (FTPFile ftpFile : files) {
            String groupName = null;
            if (pattern.matcher(ftpFile.getName()).find()) {
                groupName = ftpFile.getName().substring(0, ftpFile.getName().lastIndexOf("_"));
            } else {
                groupName = ftpFile.getName();
            }

            if (groupFileMap.get(groupName) == null) {
                groupFileMap.put(groupName, new ArrayList<FTPFile>());
            }
            groupFileMap.get(groupName).add(ftpFile);
        }
        return groupFileMap;
    }

    // 获取文件的最新版本
    private List<FTPFile> lastVersionFiles(Map<String, List<FTPFile>> ftpFileMap) {
        List<FTPFile> ftpFileList = new ArrayList<FTPFile>();
        for (Entry<String, List<FTPFile>> entry : ftpFileMap.entrySet()) {
            List<FTPFile> groupFileList = entry.getValue();
            FTPFile ftpFile = Collections.max(groupFileList, new Comparator<FTPFile>() {
                @Override
                public int compare(FTPFile file1, FTPFile file2) {
                    return file1.getName().compareToIgnoreCase(file2.getName());
                }
            });
            ftpFileList.add(ftpFile);
        }

        return ftpFileList;
    }

    /**
     * you can only create the one-level sub-directory now.
     * 
     * @param remoteDirectory
     * @param fileName
     * @param localDirectory
     */
    public void upload(String localDirectory, String fileName, String remoteDirectory) {

        FTPClient ftpClient = getFtpClient();

        try {
            ftpClient.changeWorkingDirectory(remoteDirectory);
        } catch (IOException e) {
            destroy(ftpClient);
            logger.error(Log.op("download").msg("change the directory failure").kv("directory", remoteDirectory).toString(), e);
            FtpErrorCode.CHANGE_DIRECTORY_FAILURE.exp(remoteDirectory);
        }
        File file = new File(localDirectory + File.separator + fileName);
        if (!file.exists()) {
            throw FtpErrorCode.FTP_FILE_NOT_FOUND.exp(fileName);
        }

        try (FileInputStream fis = new FileInputStream(localDirectory + File.separator + fileName); BufferedInputStream bis = new BufferedInputStream(fis)) {
            if (file.exists()) {
                logger.info(Log.op("upload").msg("upload the file starts.").kv("fileName", fileName).toString());
                ftpClient.storeFile(fileName, bis);
                logger.info(Log.op("upload").msg("upload the file finished.").kv("fileName", fileName).toString());
            }
        } catch (IOException e) {
            logger.error(Log.op("upload").msg("upload the file failue").kv("fileName", fileName).toString(), e);
            FtpErrorCode.FTP_UPLOAD_FAILURE.exp(fileName);
        } finally {
            destroy(ftpClient);
        }
    }

    /**
     * 返回目录下文件名数组
     * 
     * @return
     * @throws IOException
     */
    public String[] listNames() throws IOException {
        FTPClient ftpClient = getFtpClient();
        String[] names = ftpClient.listNames();
        destroy(ftpClient);
        return names;

    }

    /**
     * 返回FTP指定目录下的文件名数组
     * 
     * @return
     * @throws IOException
     */
    public String[] listNames(String directoryPath) throws IOException {
        FTPClient ftpClient = getFtpClient();
        ftpClient.changeWorkingDirectory(directoryPath);
        String[] names = ftpClient.listNames();
        destroy(ftpClient);
        return names;

    }

    /**
     * 返回当前工作目录
     * 
     * @return
     * @throws IOException
     */
    public String printWorkingDirectory() throws IOException {
        FTPClient ftpClient = getFtpClient();
        String workingDirectory = ftpClient.printWorkingDirectory();
        destroy(ftpClient);
        return workingDirectory;
    }

    /**
     * 切换至目标工作目录
     * 
     * @param pathname
     * @return
     * @throws IOException
     */
    public boolean changeWorkingDirectory(String pathname) throws IOException {
        FTPClient ftpClient = getFtpClient();
        boolean isSuc = ftpClient.changeWorkingDirectory(pathname);
        destroy(ftpClient);
        return isSuc;
    }

    public void closeFtpClient(FTPClient ftpClient) {
        if (ftpClient != null) {
            try {
                ftpClient.disconnect();
                logger.info(Log.op("closeFtpClient").msg("ftp disconnects successfully.").toString());
            } catch (IOException e) {
                logger.error(Log.op("closeFtpClient").msg("ftp disconnects failure.").toString(), e);
                FtpErrorCode.FTP_DISCONNECT_FAILURE.exp();
            }
        }
    }

    public void destroy(FTPClient ftpClient) {
        closeFtpClient(ftpClient);
    }

}
