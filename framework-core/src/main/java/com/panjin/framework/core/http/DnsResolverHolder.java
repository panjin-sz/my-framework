/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.http;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 *
 * @author panjin
 * @version $Id: DnsResolverHolder.java 2016年7月18日 下午4:34:37 $
 */
public class DnsResolverHolder {

    private static ArrayList<MemoryCacheDnsResolver> list = new ArrayList<MemoryCacheDnsResolver>();

    /**
     * 添加dns解析器
     * 
     * @param dnsResolver
     *            dns解析器
     */
    public static synchronized void add(MemoryCacheDnsResolver dnsResolver) {
        list.add(dnsResolver);
    }

    /**
     * 移除dns解析器
     * 
     * @param dnsResolver
     *            dns解析器
     */
    public static synchronized void remove(MemoryCacheDnsResolver dnsResolver) {
        dnsResolver.shutdown();
        list.remove(dnsResolver);
    }

    public static synchronized void shutdown() {
        Iterator<MemoryCacheDnsResolver> iter = list.iterator();
        while (iter.hasNext()) {
            MemoryCacheDnsResolver dnsResolver = iter.next();
            dnsResolver.shutdown();
            iter.remove();
        }
    }

}
