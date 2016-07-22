/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.dubbo.govern;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.InputStream;
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

import com.panjin.backend.trace.common.utils.EnvInfo;
import com.panjin.backend.trace.filters.Trace;
import com.panjin.framework.basic.log.Log;
import com.panjin.framework.core.util.DirMonitorServiceUtils;
import com.panjin.framework.core.util.DirMonitorServiceUtils.AbstractFileMonitor;

/**
 *
 *
 * @author panjin
 * @version $Id: TraceInitConfig.java 2016年7月22日 下午6:22:03 $
 */
public class TraceInitConfig {


    private static final Logger     LOG          = LoggerFactory.getLogger(TraceInitConfig.class);
    
    /**
     * 
     * @return
     */
    public static Trace getTrace(){
        return Factory.getInstance().getConfig();
    }
   
    
    /**
     * 
     * @author  
     *
     */
    public static class Factory implements ApplicationContextAware {
        
        private static final Logger logger = LoggerFactory.getLogger(Factory.class);
        private static final Factory factory = new Factory();

        private volatile static TraceInitConfig instance = null;
        private static Trace trace=Trace.getInstance();
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
         * 获取trace服务治理配置
         * 
         * @return
         */
        public Trace getConfig() {
            
            if (instance == null) {
                TraceInitConfig config;
                synchronized (lock) {
                    config = instance;
                    if (config == null) {
                        synchronized (lock) {
                            try {
                                if (applicationContext != null) {
                                    config = applicationContext.getBean(TraceInitConfig.class);
                                }
                            } catch (BeansException ex) {
                                logger.error(Log.op("sensinfo_crypto_config_not_found")
                                          .msg("SensInfoCryptoConfig_bean_not_exists")
                                          .kv("className", Factory.class.getName()).kv("method", "getConfig")
                                          .kv("exception", ex.getMessage()).toString());
                            }
                            if (config == null) {
                                config = new TraceInitConfig();
                            }

                            if (config != null) {
                                try {
                                    //Path initFile = Paths.get(TraceInitConfig.class.getClassLoader().getResource("trace.properties").toURI()); 
                                    initConfig(trace/*,initFile*/);
                                    TraceConfigFileMonitor monitor = new TraceConfigFileMonitor();
                                    //监控根目录 
                                    Path path = Paths.get(TraceInitConfig.class.getClassLoader().getResource("").toURI()); 
                                    DirMonitorServiceUtils.watch(path, monitor);
                                } catch (URISyntaxException e) {
                                    logger.error(Log.op("create_watcher_error")
                                              .msg("create_DirWatchServiceUtils_watchservice_fail")
                                              .kv("className", Factory.class.getName()).kv("method", "getConfig")
                                              .kv("exception", e.getMessage()).toString(), e);
                                }
                            }
                        }
                        instance = config;
                    }
                }
            }
            return trace;
        }
 
    }
    
    /**
     * 备注：文件存在jar包中或者类路径中，直接用class。getClassLoader().getResourceAsStream(fileName)可以获取输入流
     * @param trace
     */
    private  static void initConfig(Trace trace/*,Path resolvePath*/) {
        LOG.info(Log.op("init trace filter").msg("init trace filter begin")
                                  .kv("envInfo", EnvInfo.getEnvInfo()).toString());
        Properties properties = new Properties();
        try (InputStream fis = TraceInitConfig.class.getClassLoader().getResourceAsStream("trace.properties");){
            properties.load(fis);
            trace.init(properties);

        } catch (Exception e) {
            // 若是配置文件不存在的话，那么就全是默认值
            properties = new Properties();
            trace.init(properties);
        }
        LOG.info(Log.op("init trace filter").msg("init trace filter end")
                              .kv("envInfo", EnvInfo.getEnvInfo()).toString());
    }

    /**
     * 监视器
     * @author liangzheng
     *
     */
    static class TraceConfigFileMonitor extends  AbstractFileMonitor {

        @Override
        public boolean match(String fileName, String fileAbsolutePath, Kind<Path> kind) {
            if ((ENTRY_CREATE.equals(kind) || ENTRY_MODIFY.equals(kind)) && "trace.properties".equals(fileName)) {
                return true;
            }
            return false;
        }

        @Override
        public void process(Path file, Path resolvePath, Kind<Path> eventKind) {
            initConfig(getTrace()/*,resolvePath*/);
        }
    }
 

}
