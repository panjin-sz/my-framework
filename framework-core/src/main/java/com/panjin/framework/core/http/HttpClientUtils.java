/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panjin.framework.basic.log.Log;
import com.panjin.framework.basic.log.LogOp;
import com.panjin.framework.core.util.ValidateUtils;

/**
 *
 *
 * @author panjin
 * @version $Id: HttpClientUtils.java 2016年7月18日 下午4:28:01 $
 */
public class HttpClientUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static final String ENCODING = "UTF-8";

    /**
     * 是否是使用默认的单连接的连接池
     */
    private static boolean simpleConnection = true;

    private static HttpClient client = null;

    /**
     * MB
     */
    public static final int MB = 1024 * 1024;

    /**
     * 使用默认的配置初始化连接池
     */
    public static void init() {
        init(new ClientConfiguration());
    }

    /**
     * 使用指定的超时等配置初始化连接池(时间单位:毫秒)
     * 
     * @param timeout
     *            超时时间
     * @param maxConns
     *            最大连接数
     * @param maxPerRoute
     *            每个host支持的最大连接数
     */
    public static void init(int timeout, int maxConns, int maxPerRoute) {
        ClientConfiguration clientConfig = new ClientConfiguration();
        if (clientConfig.isNeedTimeout()) {
            clientConfig.setConnectionTimeout(timeout);
            clientConfig.setSocketTimeout(timeout);
            clientConfig.setMaxConnections(maxConns);
            clientConfig.setMaxPreRoute(maxPerRoute);
        }
        init(clientConfig);
    }

    /**
     * 根据指定配置初始化连接池
     * 
     * @param clientConfig
     *            客户端配置
     */
    public static synchronized void init(ClientConfiguration clientConfig) {
        try {
            if (client != null) {
                return;
            }
            client = HttpClientFactory.createHttpClient(clientConfig);
            logger.warn(Log.op(LogOp.HTTP_POOL_INIT).msg("init httpClient connection pool").kv("timeout", clientConfig.getConnectionTimeout())
                    .kv("maxConns", clientConfig.getMaxConnections()).kv("maxPerRoute", clientConfig.getMaxPreRoute()).toString());
            simpleConnection = false;
        } catch (Exception ex) {
            logger.error(Log.op(LogOp.HTTP_POOL_INIT).msg("init http client fail").toString(), ex);
        }
    }

    /**
     * 获取HttpClient对象
     * 
     * @return
     */
    private static HttpClient createHttpClient() {
        // 若不使用连接池，则创建1个HttpClient对象
        if (client == null) {
            return HttpClientFactory.createDefaultHttpClient();
        }
        return client;
    }

    /**
     * 关闭连接池
     */
    public static synchronized void shutdown() {
        logger.warn(Log.op(LogOp.HTTP_POOL_SHUT).msg("shutdown httpClient connection pool.").toString());
        if (client != null) {
            client.getConnectionManager().shutdown();
            IdleConnectionReaper.shutdown();
            DnsResolverHolder.shutdown();
            client = null;
        }
    }

    /**
     * 关闭默认的httpClient实例
     * 
     * @param httpClient
     * @param httpResponse
     */
    private static void shutdownHttpClient(HttpClient httpClient, HttpResponse httpResponse) {
        try {
            if (httpResponse != null) {
                EntityUtils.consume(httpResponse.getEntity());
            }
            if (simpleConnection) {
                httpClient.getConnectionManager().shutdown();
            }
        } catch (Exception ex) {
            logger.error(Log.op(LogOp.HTTP_CONN_SHUT_FAIL).toString(), ex);
        }
    }

    /**
     * httpRequest
     * 
     * @param url
     *            请求url
     * @param method
     *            请求方法
     * @param paramMap
     *            请求参数
     * @throws IllegalArgumentException
     * @return
     */
    public static Response execute(String url, String method, Map<String, String> paramMap) {
        ValidateUtils.ensureParamNotNullEmpty("url", url);
        ValidateUtils.ensureParamNotNullEmpty("method", method);
        if ("post".equalsIgnoreCase(method)) {
            return post(url, paramMap);
        } else if ("get".equalsIgnoreCase(method)) {
            return get(url, paramMap);
        } else if ("delete".equalsIgnoreCase(method)) {
            return delete(url, paramMap);
        }
        throw new IllegalArgumentException("method not support!method=" + method);
    }

    /**
     * post请求
     * 
     * @param url
     *            请求url
     * @param content
     *            请求的内容
     * @return
     */
    public static Response post(String url, String content) {
        return post(url, content, null, ENCODING);
    }

    /**
     * post请求
     * 
     * @param url
     *            请求url
     * @param content
     *            请求的内容
     * @param encoding
     *            内容编码
     * @return
     */
    public static Response post(String url, String content, String encoding) {
        return post(url, content, null, encoding);
    }

    /**
     * 
     * @param url
     *            请求url
     * @param content
     *            请求内容
     * @param headerMap
     *            请求的http header
     * @return
     */
    public static Response post(String url, String content, Map<String, String> headerMap) {
        return post(url, content, headerMap, ENCODING);
    }

    /**
     * Post方法，body采用ByteArrayEntity
     * 
     * @param url
     *            请求url
     * @param content
     *            请求内容
     * @param headerMap
     *            请求的http header
     * @param encoding
     *            内容编码
     * @return
     */
    public static Response post(String url, String content, Map<String, String> headerMap, String encoding) {
        ValidateUtils.ensureParamNotNullEmpty("url", url);
        Response response = new Response();
        long startTime = System.currentTimeMillis();

        HttpClient httpClient = createHttpClient();
        HttpResponse httpResponse = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            if (ValidateUtils.isNotNullEmpty(content)) {
                httpPost.setEntity(new ByteArrayEntity(content.getBytes(encoding)));
            }
            if (headerMap != null) {
                for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                    httpPost.addHeader(headerEntry.getKey(), headerEntry.getValue());
                }
            }
            logger.debug(Log.op(LogOp.HTTP_REQ_START).toString());

            httpResponse = httpClient.execute(httpPost);
            response.code = httpResponse.getStatusLine().getStatusCode();
            response.content = EntityUtils.toString(httpResponse.getEntity(), encoding);

            logger.debug(Log.op(LogOp.HTTP_REQ_FINISH).kv("responseCode", response.code).kv("content", response.content).toString());
        } catch (Exception e) {
            logger.error(Log.op(LogOp.HTTP_REQ_FAIL).toString(), e);
            response.errorMsg = e.getMessage();
        } finally {
            shutdownHttpClient(httpClient, httpResponse);
        }
        response.reqTime = System.currentTimeMillis() - startTime;
        return response;
    }

    /**
     * post请求
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @return
     */
    public static Response post(String url, Map<String, String> paramMap) {
        Map<String, String> headerMap = Collections.emptyMap();
        return post(url, paramMap, headerMap, ENCODING);
    }

    /**
     * post请求
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @param encoding
     *            内容编码
     * @return
     */
    public static Response post(String url, Map<String, String> paramMap, String encoding) {
        return post(url, paramMap, null, encoding);
    }

    /**
     * post请求
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @param headerMap
     *            请求的http header
     * @return
     */
    public static Response post(String url, Map<String, String> paramMap, Map<String, String> headerMap) {
        return post(url, paramMap, null, ENCODING);
    }

    /**
     * post方法，body采用UrlEncodedFormEntity
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @param headerMap
     *            请求的http header
     * @param encoding
     *            请求编码
     * @return
     */
    public static Response post(String url, Map<String, String> paramMap, Map<String, String> headerMap, String encoding) {
        ValidateUtils.ensureParamNotNullEmpty("url", url);

        Response response = new Response();
        long startTime = System.currentTimeMillis();

        HttpClient httpClient = createHttpClient();
        HttpResponse httpResponse = null;
        try {
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
                    NameValuePair nameValuePair = new BasicNameValuePair(paramEntry.getKey(), paramEntry.getValue());
                    nameValuePairs.add(nameValuePair);
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, encoding));
            }

            if (headerMap != null) {
                for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                    httpPost.addHeader(headerEntry.getKey(), headerEntry.getValue());
                }
            }
            logger.debug(Log.op(LogOp.HTTP_REQ_START).toString());

            httpResponse = httpClient.execute(httpPost);
            response.code = httpResponse.getStatusLine().getStatusCode();
            response.content = EntityUtils.toString(httpResponse.getEntity(), encoding);

            logger.debug(Log.op(LogOp.HTTP_REQ_FINISH).kv("responseCode", response.code).kv("content", response.content).toString());
        } catch (Exception e) {
            logger.error(Log.op(LogOp.HTTP_REQ_FAIL).toString(), e);
            response.errorMsg = e.getMessage();
        } finally {
            shutdownHttpClient(httpClient, httpResponse);
        }
        response.reqTime = System.currentTimeMillis() - startTime;
        return response;
    }

    /**
     * post方法上传文件
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @param filePath
     *            文件路径
     * @return
     */
    public static Response postFile(String url, Map<String, String> paramMap, String filePath) {
        ValidateUtils.ensureParamNotNullEmpty("url", url);

        Response response = new Response();
        long startTime = System.currentTimeMillis();

        HttpClient httpClient = createHttpClient();
        HttpResponse httpResponse = null;
        try {
            HttpPost httpPost = new HttpPost(url);

            FileBody bin = null;
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("fileNotExist!");
            }
            if (file != null) {
                bin = new FileBody(file);
            }
            // browser-compatible mode:only write Content-Disposition; use
            // content charset
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName(ENCODING));
            reqEntity.addPart(file.getName(), bin);

            if (paramMap != null) {
                for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
                    StringBody item = new StringBody(paramEntry.getValue(), Charset.forName(ENCODING));
                    reqEntity.addPart(paramEntry.getKey(), item);
                }
            }
            httpPost.setEntity(reqEntity);

            logger.debug(Log.op(LogOp.HTTP_REQ_START).toString());
            httpResponse = httpClient.execute(httpPost);
            response.code = httpResponse.getStatusLine().getStatusCode();
            response.content = EntityUtils.toString(httpResponse.getEntity(), ENCODING);

            logger.debug(Log.op(LogOp.HTTP_REQ_FINISH).kv("responseCode", response.code).kv("content", response.content).toString());
        } catch (Exception e) {
            logger.error(Log.op(LogOp.HTTP_REQ_FAIL).toString(), e);
            response.errorMsg = e.getMessage();
        } finally {
            shutdownHttpClient(httpClient, httpResponse);
        }
        response.reqTime = System.currentTimeMillis() - startTime;
        return response;
    }

    /**
     * delete请求
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @return
     */
    public static Response delete(String url, Map<String, String> paramMap) {
        return delete(url, paramMap, ENCODING);
    }

    /**
     * delete请求
     * 
     * @param url
     *            请求url
     * @param paramMap
     *            请求参数
     * @param encoding
     *            内容编码
     * @return
     */
    public static Response delete(String url, Map<String, String> paramMap, String encoding) {
        ValidateUtils.ensureParamNotNullEmpty("url", url);

        Response response = new Response();
        long startTime = System.currentTimeMillis();

        HttpClient httpClient = createHttpClient();
        try {
            logger.debug(Log.op(LogOp.HTTP_REQ_START).toString());

            HttpDelete del = new HttpDelete(appendParameter2Url(url, paramMap, encoding));
            httpExecute(httpClient, del, response, encoding);

            logger.debug(Log.op(LogOp.HTTP_REQ_FINISH).kv("responseCode", response.code).kv("content", response.content).toString());
        } catch (Exception ex) {
            logger.error(Log.op(LogOp.HTTP_REQ_FAIL).toString(), ex);
            response.errorMsg = ex.getMessage();
        } finally {
            shutdownHttpClient(httpClient, null);
        }
        response.reqTime = System.currentTimeMillis() - startTime;
        return response;
    }

    public static Response get(String url, Map<String, String> paramMap) {
        return get(url, paramMap, ENCODING);
    }

    public static Response get(String url, Map<String, String> paramMap, String encoding) {
        ValidateUtils.ensureParamNotNullEmpty("url", url);

        Response response = new Response();
        long startTime = System.currentTimeMillis();

        HttpClient httpClient = createHttpClient();
        try {
            logger.debug(Log.op(LogOp.HTTP_REQ_START).toString());

            HttpGet get = new HttpGet(appendParameter2Url(url, paramMap, encoding));
            httpExecute(httpClient, get, response, encoding);

            logger.debug(Log.op(LogOp.HTTP_REQ_FINISH).kv("responseCode", response.code).kv("content", response.content).toString());
        } catch (Exception ex) {
            logger.error(Log.op(LogOp.HTTP_REQ_FAIL).toString(), ex);
            response.errorMsg = ex.getMessage();
        } finally {
            shutdownHttpClient(httpClient, null);
        }
        response.reqTime = System.currentTimeMillis() - startTime;
        return response;
    }

    public static Response getByStream(String url, Map<String, String> paramMap) {
        return getByStream(url, paramMap, ENCODING);
    }

    public static Response getByStream(String url, Map<String, String> paramMap, String encoding) {
        Response response = new Response();

        ValidateUtils.ensureParamNotNullEmpty("url", url);
        long startTime = System.currentTimeMillis();

        HttpClient httpClient = createHttpClient();
        try {
            logger.debug(Log.op(LogOp.HTTP_REQ_START).toString());

            HttpGet get = new HttpGet(appendParameter2Url(url, paramMap, encoding));
            httpExecuteByStream(httpClient, get, response);

            logger.debug(Log.op(LogOp.HTTP_REQ_FINISH).kv("responseCode", response.code).kv("content", response.content).toString());
        } catch (Exception ex) {
            logger.error(Log.op(LogOp.HTTP_REQ_FAIL).toString(), ex);
            response.errorMsg = ex.getMessage();
        } finally {
            shutdownHttpClient(httpClient, null);
        }
        response.reqTime = System.currentTimeMillis() - startTime;
        return response;
    }

    private static void httpExecute(HttpClient httpClient, HttpUriRequest request, Response resp, String encoding) throws Exception {
        HttpEntity entity = null;
        HttpResponse response;
        try {
            response = httpClient.execute(request);
            entity = response.getEntity();
            String ret = EntityUtils.toString(entity, encoding);
            resp.code = response.getStatusLine().getStatusCode();
            resp.content = ret;
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                }
            }
        }
    }

    private static void httpExecuteByStream(HttpClient httpClient, HttpUriRequest request, Response resp) throws Exception {
        HttpEntity entity = null;
        HttpResponse response;
        try {
            response = httpClient.execute(request);
            entity = response.getEntity();
            InputStream is = entity.getContent();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[MB];
            int read = -1;
            while ((read = is.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            resp.code = response.getStatusLine().getStatusCode();
            resp.data = bos.toByteArray();
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 将参数拼接到url后面
     * 
     * @param url
     * @param params
     */
    private static String appendParameter2Url(String url, Map<String, ?> params, String encoding) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, ?> paramEntry : params.entrySet()) {
            String value = (String) paramEntry.getValue();
            NameValuePair nameValuePair = new BasicNameValuePair(paramEntry.getKey(), value);
            nameValuePairs.add(nameValuePair);
        }
        String retUrl = url;
        if (!url.contains("?")) {
            retUrl += "?";
        }
        return retUrl + URLEncodedUtils.format(nameValuePairs, encoding);
    }

}
