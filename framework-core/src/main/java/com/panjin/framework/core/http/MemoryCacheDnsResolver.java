/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.conn.DnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panjin.framework.basic.log.Log;
import com.panjin.framework.basic.log.LogOp;

/**
 *
 *
 * @author panjin
 * @version $Id: MemoryCacheDnsResolver.java 2016年7月18日 下午4:32:19 $
 */
public class MemoryCacheDnsResolver implements DnsResolver {

    private final static Logger logger = LoggerFactory.getLogger(MemoryCacheDnsResolver.class);

    // 缓存解析结果
    private ConcurrentMap<String, InetAddress[]> dnsMap;

    // 负责解析域名的线程
    private Thread resolveThread;

    // 负责解析域名的线程名称
    private static final String THREAD_NAME = "httpclient-dns-resolver";

    // 域名更新时间
    public static final long updateTime = 30 * 1000;

    // 是否使用缓存
    private volatile boolean useCache = true;

    public MemoryCacheDnsResolver() {
        dnsMap = new ConcurrentHashMap<String, InetAddress[]>();
        resolveThread = new Thread(new HostResolver(), THREAD_NAME);
        resolveThread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.http.conn.DnsResolver#resolve(java.lang.String)
     */
    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        // 不使用缓存则直接系统解析
        if (!useCache) {
            return resolvedBySystem(host);
        }
        InetAddress[] resolvedAddresses = dnsMap.get(host);
        // 这里不额外加些同步是觉得没什么必要
        if (resolvedAddresses == null) {
            resolvedAddresses = resolvedBySystem(host);

            if (resolvedAddresses == null) {
                throw new UnknownHostException(host + " cannot be resolved");
            }

            dnsMap.put(host, resolvedAddresses);
        }

        return resolvedAddresses;
    }

    /**
     * 调用系统接口解析dns
     * 
     * @param host
     * @return
     * @throws UnknownHostException
     */
    private InetAddress[] resolvedBySystem(String host) {
        InetAddress[] resolvedAddresses = null;
        try {
            resolvedAddresses = InetAddress.getAllByName(host);
        } catch (Exception e) {
            logger.error(Log.op(LogOp.HTTP_DNS_RESOLVE_FAIL).toString(), e);
            return resolvedAddresses;
        }
        logger.debug(Log.op(LogOp.HTTP_DNS_RESOLVE_SUC).kv("host", host).kv("address", Arrays.deepToString(resolvedAddresses)).toString());
        return resolvedAddresses;
    }

    /**
     * 主要是用来关闭HostResolver线程的
     */
    public void shutdown() {
        this.useCache = false;
        resolveThread.interrupt();
    }

    class HostResolver implements Runnable {
        @Override
        public void run() {
            while (useCache) {
                updateCache();

                // 更新线程退出的话，则不再使用缓存了
                try {
                    Thread.sleep(updateTime);
                } catch (InterruptedException e) {
                    useCache = false;
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        /**
         * 更新缓存的域名的映射
         */
        public void updateCache() {
            // TODO 将dnsMap改成带过期时间的map，否则对于那种动态域名很多的场景容易导致内存泄露
            for (String host : dnsMap.keySet()) {
                InetAddress[] newAddress = resolvedBySystem(host);
                if (newAddress != null) {
                    dnsMap.put(host, newAddress);
                }
            }
        }
    }
}
