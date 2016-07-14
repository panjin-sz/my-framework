/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.log;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.panjin.framework.basic.log.Log;
import com.panjin.framework.core.util.DirMonitorServiceUtils;
import com.panjin.framework.core.util.DirMonitorServiceUtils.AbstractFileMonitor;

/**
 * 敏感信息日志加密配置
 *
 * @author panjin
 * @version $Id: SensInfoLogCryptoConfig.java 2016年7月14日 下午2:53:28 $
 */
public final class SensInfoLogCryptoConfig {

    private static final Logger logger = LoggerFactory.getLogger(SensInfoLogCryptoConfig.class);
    private String sensInfoCryptoEnable = "true";
    private boolean isSensInfoCryptoEnable = Boolean.TRUE;

    private SensInfoLogCryptoConfig() {
    }

    public Boolean isSensInfoCryptoEnable() {
        return isSensInfoCryptoEnable;
    }

    /**
     * 
     * @param sensInfoCryptoEnable
     *            是否启用敏感信息加密
     */
    public void setSensInfoCryptoEnable(String sensInfoCryptoEnable) {
        this.sensInfoCryptoEnable = sensInfoCryptoEnable;
        isSensInfoCryptoEnable = StringUtils.isEmpty(this.sensInfoCryptoEnable) ? Boolean.TRUE : Boolean.valueOf(this.sensInfoCryptoEnable);
        Log.setSansInfoCryptoEnable(this.isSensInfoCryptoEnable());
    }

    public static SensInfoLogCryptoConfig getConfig() {
        return Factory.getInstance().getConfig();
    }

    /**
     * 
     */
    public static class Factory implements ApplicationContextAware {
        private static final Logger logger = LoggerFactory.getLogger(Factory.class);
        private static final Factory factory = new Factory();

        private volatile static SensInfoLogCryptoConfig instance = null;
        private static final Object lock = new Object();

        private static ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext context) throws BeansException {
            applicationContext = context;
            this.getConfig();
        }

        public static Factory getInstance() {
            return factory;
        }

        /**
         * 获取敏感信息加密配置
         * 
         * @return
         */
        public SensInfoLogCryptoConfig getConfig() {
            if (instance == null) {
                SensInfoLogCryptoConfig config;
                synchronized (lock) {
                    config = instance;
                    if (config == null) {
                        synchronized (lock) {
                            try {
                                if (applicationContext != null) {
                                    config = applicationContext.getBean(SensInfoLogCryptoConfig.class);
                                }
                            } catch (BeansException ex) {
                                logger.error(Log.op("sensinfo_crypto_config_not_found").msg("SensInfoCryptoConfig_bean_not_exists")
                                        .kv("className", Factory.class.getName()).kv("method", "getConfig").kv("exception", ex.getMessage()).toString());
                            }
                            if (config == null) {
                                config = new SensInfoLogCryptoConfig();
                            }
                            if (config != null) {
                                try {
                                    SensInfoLogCryptoConfigFileMonitor monitor = new SensInfoLogCryptoConfigFileMonitor(config);
                                    // 监控根目录
                                    Path path = Paths.get(SensInfoLogCryptoConfig.class.getClassLoader().getResource("").toURI());
                                    DirMonitorServiceUtils.watch(path, monitor);
                                } catch (URISyntaxException e) {
                                    logger.error(
                                            Log.op("create_watcher_error").msg("create_DirWatchServiceUtils_watchservice_fail")
                                                    .kv("className", Factory.class.getName()).kv("method", "getConfig").kv("exception", e.getMessage())
                                                    .toString(), e);
                                }
                            }
                        }
                        instance = config;
                    }
                }
            }
            return instance;
        }
    }

    /**
     * senslog.properties文件变更监控
     *
     */
    static class SensInfoLogCryptoConfigFileMonitor extends AbstractFileMonitor {
        private SensInfoLogCryptoConfig config;

        public SensInfoLogCryptoConfigFileMonitor(SensInfoLogCryptoConfig config) {
            this.config = config;
        }

        @Override
        public boolean match(String fileName, String fileAbsolutePath, Kind<Path> kind) {
            if ((ENTRY_CREATE.equals(kind) || ENTRY_MODIFY.equals(kind)) && "senslog.properties".equals(fileName)) {
                return true;
            }
            return false;
        }

        @Override
        public void process(Path file, Path resolvePath, Kind<Path> eventKind) {
            logger.info(Log.op("senslog.properties_create_or_modify").kv("file_path", resolvePath.toString()).kv("operation", eventKind.name()).toString());
            try (FileInputStream fis = new FileInputStream(resolvePath.toFile());) {
                Properties properties = new Properties();
                properties.load(fis);
                String isCryptoEnable = properties.getProperty("sensinfo.crypto.enable");
                config.setSensInfoCryptoEnable(isCryptoEnable);
            } catch (IOException e) {
                logger.error(
                        Log.op("load_senslog.properties_io_exception").msg(e.getMessage()).kv("className", SensInfoLogCryptoConfigFileMonitor.class.getName())
                                .kv("method", "process").toString(), e);
            }
        }

    }
}
