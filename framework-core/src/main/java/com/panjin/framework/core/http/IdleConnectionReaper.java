/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panjin.framework.basic.log.Log;
import com.panjin.framework.basic.log.LogOp;

/**
 *
 *
 * @author panjin
 * @version $Id: IdleConnectionReaper.java 2016年7月18日 下午4:36:44 $
 */
public class IdleConnectionReaper extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(IdleConnectionReaper.class);

    // 检查连接的时间间隔
    private static final int PERIOD_MILLISECONDS = 1000 * 60 * 1;

    // 需要管理的连接管理器
    private static ArrayList<ClientConnectionManager> connectionManagers = new ArrayList<ClientConnectionManager>();

    // 单例
    private static IdleConnectionReaper instance;

    /**
     * 回收线程的名字
     */
    public static final String THREAD_NAME = "IdleConnectionReaper";

    private IdleConnectionReaper(String threadName) {
        setName(threadName);
        setDaemon(true);
        start();
    }

    public static synchronized void registerConnectionManager(ClientConnectionManager connectionManager) {
        if (instance == null) {
            instance = new IdleConnectionReaper(THREAD_NAME);
        }
        logger.info(Log.op(LogOp.HTTP_POOL_REG).kv("cm", connectionManager).toString());
        connectionManagers.add(connectionManager);
    }

    public static synchronized void removeConnectionManager(ClientConnectionManager connectionManager) {
        connectionManagers.remove(connectionManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(PERIOD_MILLISECONDS);

                List<ClientConnectionManager> connectionManagers = null;
                synchronized (IdleConnectionReaper.class) {
                    connectionManagers = (List<ClientConnectionManager>) IdleConnectionReaper.connectionManagers.clone();
                }
                for (ClientConnectionManager connectionManager : connectionManagers) {
                    try {
                        logBeforeClean(connectionManager);

                        // Close expired connections
                        connectionManager.closeExpiredConnections();
                        // 如果连接空闲超过60秒,就会被关闭
                        connectionManager.closeIdleConnections(60, TimeUnit.SECONDS);

                        logAfterClean(connectionManager);
                    } catch (Throwable t) {
                        throw new RuntimeException("Unable to close idle connections", t);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void logBeforeClean(ClientConnectionManager connectionManager) {
        if (connectionManager instanceof PoolingClientConnectionManager) {
            PoolingClientConnectionManager connManager = (PoolingClientConnectionManager) connectionManager;
            PoolStats ps = connManager.getTotalStats();
            logger.debug(Log.op(LogOp.HTTP_POOL_CLEAN).kv("PoolStats", ps).toString());
        }
    }

    private void logAfterClean(ClientConnectionManager connectionManager) {
        if (connectionManager instanceof PoolingClientConnectionManager) {
            PoolingClientConnectionManager connManager = (PoolingClientConnectionManager) connectionManager;
            PoolStats ps = connManager.getTotalStats();
            logger.debug(Log.op(LogOp.HTTP_POOL_CLEAN).kv("PoolStats", ps).toString());
        }
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.interrupt();
            connectionManagers.clear();
            instance = null;
        }
    }

}
