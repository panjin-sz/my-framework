/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.http;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 *
 *
 * @author panjin
 * @version $Id: HttpClientFactory.java 2016年7月18日 下午4:29:52 $
 */
public class HttpClientFactory {

    /**
     * 新建默认的HttpClient对象(DefaultHttpClient)
     * 
     * @return
     */
    public static HttpClient createDefaultHttpClient() {
        HttpParams clientParams = new BasicHttpParams();
        clientParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, ClientConfiguration.DEFAULT_ENCODING);
        clientParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT);
        clientParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT);

        DefaultHttpClient httpClient = new DefaultHttpClient(clientParams);
        return httpClient;
    }

    /**
     * 使用默认的客户端配置，创建带连接池的HttpClient对象
     * 
     * @return
     */
    public static HttpClient createHttpClient() {
        return createHttpClient(new ClientConfiguration());
    }

    /**
     * 使用指定的客户端配置，创建带连接池的HttpClient对象
     * 
     * @param config
     *            客户端配置
     * @return
     */
    public static HttpClient createHttpClient(ClientConfiguration config) {
        // 设置参数
        HttpParams clientParams = new BasicHttpParams();
        if (config.isNeedTimeout()) {
            HttpConnectionParams.setConnectionTimeout(clientParams, config.getConnectionTimeout());
            HttpConnectionParams.setSoTimeout(clientParams, config.getSocketTimeout());
        }
        HttpConnectionParams.setStaleCheckingEnabled(clientParams, config.isStaleCheck());
        HttpConnectionParams.setTcpNoDelay(clientParams, config.isTcpNoDelay());
        int soSendBufSize = config.getSocketSendBufferSize();
        int soRecvBufSize = config.getSocketReceiveBufferSize();
        if (soSendBufSize > 0 || soRecvBufSize > 0) {
            HttpConnectionParams.setSocketBufferSize(clientParams, Math.max(soSendBufSize, soRecvBufSize));
        }

        // 设置connection manager
        PoolingClientConnectionManager connManager = ConnectionManagerFactory.createClientConnManager(config, clientParams);
        DefaultHttpClient httpClient = new DefaultHttpClient(connManager, clientParams);

        // 设置代理
        String proxyHost = config.getProxyHost();
        int proxyPort = config.getProxyPort();
        if (proxyHost != null && proxyPort > 0) {
            HttpHost proxyHttpHost = new HttpHost(proxyHost, proxyPort);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);

            String proxyUserName = config.getProxyUserName();
            String proxyPassword = config.getProxyPassword();
            String proxyDomain = config.getProxyDomain();
            String proxyWorkstation = config.getProxyWorkstation();

            if (proxyUserName != null && proxyPassword != null) {
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxyHost, proxyPort),
                        new NTCredentials(proxyUserName, proxyPassword, proxyWorkstation, proxyDomain));
            }
        }

        return httpClient;
    }

}
