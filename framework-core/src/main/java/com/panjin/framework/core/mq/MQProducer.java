/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.mq;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.panjin.backend.trace.filters.support.TraceClientConst;
import com.panjin.backend.trace.filters.utils.TraceWebUtils;
import com.panjin.backend.trace.meta.model.Endpoint;
import com.panjin.backend.trace.meta.model.Span;
import com.panjin.cloud.nqs.client.ClientConfig;
import com.panjin.cloud.nqs.client.Message;
import com.panjin.cloud.nqs.client.exception.MessageClientException;
import com.panjin.cloud.nqs.client.producer.ProducerConfig;
import com.panjin.cloud.nqs.client.push.PushMessageClient;
import com.panjin.framework.basic.log.Log;
import com.panjin.framework.basic.log.LogOp;
import com.panjin.framework.core.util.CoreUtils;
import com.rabbitmq.client.ShutdownSignalException;

/**
 *
 *
 * @author panjin
 * @version $Id: MQProducer.java 2016年7月22日 下午6:26:17 $
 */
public class MQProducer extends MQTraceUtil {
    private MQConfig mqConfig;

    private MQProducerConfig mqProducerConfig;

    private volatile PushMessageClient pushMessageClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int DEFAULT_RETRY_TIMES = 3;

    private int retryTimes = DEFAULT_RETRY_TIMES;

    private final static long RETRY_INTERVAL = 500;

    /**
     * 新建生产者
     * 
     * @param mqConfig
     *            RabbitMQ配置
     * @param mqProducerConfig
     *            生产者配置
     */
    public MQProducer(MQConfig mqConfig, MQProducerConfig mqProducerConfig) {
        this.mqConfig = mqConfig;
        this.mqProducerConfig = mqProducerConfig;
    }

    /**
     * 初始化
     */
    public void init() {
        if (mqConfig == null) {
            throw new MQException("mqConfig can not be null");
        }
        if (mqProducerConfig == null) {
            throw new MQException("mqProducerConfig can not be null");
        }
        init(mqConfig, mqProducerConfig);
    }

    private synchronized void init(MQConfig mqConfig, MQProducerConfig mqProducerConfig) {
        if (pushMessageClient != null) {
            return;
        }
        ClientConfig cc = mqConfig.getClientConfig();

        ProducerConfig pc = new ProducerConfig();
        pc.setProductId(mqConfig.getExchange());
        pc.setQueueName("useless");
        pc.setRequireConfirm(mqProducerConfig.isRequireConfirm());
        pc.setWaitTimeout(mqProducerConfig.getConfirmTimeout());

        try {
            pushMessageClient = new PushMessageClient(cc, pc);
        } catch (MessageClientException e) {
            throw new MQException("init mq producer fail", e);
        }
        logger.info(Log.op(LogOp.MQ_PRODUCER_INIT).msg("init suc").toString());
    }

    /**
     * 发送消息得某个队列
     * 
     * @param message
     *            要发送的消息
     * @param routingKey
     *            发送的队列(路由键)
     */
    public void sendMessage(String message, String routingKey) {
        int retry = 0;
        boolean connShutdowned = false;
        boolean sendSuc = false;
        while (!sendSuc && retry++ <= retryTimes) {
            try {
                if (connShutdowned) {
                    init();
                }
                Message mess = new Message(message.getBytes("UTF-8"), true);
                pushMessageClient.sendMessageWithRoutingKey(mess, routingKey);
                sendSuc = true;
            } catch (ShutdownSignalException ex) {
                logger.warn(Log.op(LogOp.MQ_PRODUCER_FAIL).msg("shutdownSignal").kv("errorMsg", ex.getMessage()).toString());
                destory();
                retry++;
                connShutdowned = true;
                CoreUtils.sleep(RETRY_INTERVAL);
            } catch (Exception ex) {
                logger.warn(Log.op(LogOp.MQ_PRODUCER_FAIL).msg("exception occur").kv("errorMsg", ex.getMessage()).toString(), ex);
                destory();
                retry++;
                connShutdowned = true;
                CoreUtils.sleep(RETRY_INTERVAL);
            }
        }
        if (!sendSuc) {
            throw new MQException("send mq message fail");
        }
    }

    /**
     * 发送消息得某个队列
     * 
     * @param message
     *            要发送的消息
     * @param routingKey
     *            发送的队列(路由键)
     */
    public void sendMessageWithTraceId(String message, String routingKey) {
        /**
         * if (trace==null||!trace.isOn()) { sendMessage(message,routingKey); }
         */
        String spanName = mqConfig.getExchange() + "." + routingKey;
        sendWithTrace(message, routingKey, spanName);
    }

    public void sendMessageWithTraceId(String identify, String message, String routingKey) {

        String spanName = mqConfig.getExchange() + "." + routingKey + "." + identify;
        sendWithTrace(message, routingKey, spanName);
    }

    private void sendWithTrace(String message, String routingKey, String spanName) {
        RpcContext context = RpcContext.getContext();
        boolean consumerSide = true;
        Span span = null;// 本次调用的span
        Endpoint endpoint = new Endpoint(TraceWebUtils.getIPAddress(), TraceWebUtils.getHostName(), context.getLocalPort());

        span = buildSpan(consumerSide, spanName, null);
        span.setHost(endpoint);
        span.setAppName(trace.getAppName());

        startInvoke(span, consumerSide);
        // 要把span放置到消息中
        Preconditions.checkNotNull(span, "span is null:" + span);
        MQTransInfo info = new MQTransInfo();
        info.setMessage(message);
        getConcurrent(spanName).incrementAndGet(); // 并发计数
        Map<String, String> attachments = new HashMap<String, String>();
        // 调用其他Dubbo服务必须设置是否采样
        attachments.put(TraceClientConst.IS_SAMPLE, String.valueOf(span.isSample()));
        attachments.put(TraceClientConst.SPAN_ID, String.valueOf(span.getId()));
        attachments.put(TraceClientConst.PARENT_ID, String.valueOf(span.getParentId()));
        attachments.put(TraceClientConst.RPC_ID, String.valueOf(span.getRpcId()));
        attachments.put(TraceClientConst.TRACE_ID, String.valueOf(span.getTraceId()));
        attachments.put(MQTraceUtil.spanName, String.valueOf(spanName));
        info.setAttachments(attachments);
        try {
            this.sendMessage(JSON.toJSONString(info), routingKey);
        } finally {
            endInvoke(span, consumerSide);
            // 记录并发数据
            int concurrent = getConcurrent(spanName).get(); // 当前并发数
            trace.logConcurrent(span, concurrent);
            // Log span
            trace.logSpan(span);
            getConcurrent(spanName).decrementAndGet(); // 并发计数
        }
    }

    /**
     * 销毁生产者
     */
    public synchronized void destory() {
        if (pushMessageClient != null) {
            logger.info(Log.op(LogOp.MQ_PRODUCER_SHUTDOWN).msg("destory start").toString());
            try {
                pushMessageClient.shutdown();
            } catch (ShutdownSignalException ex) {
                logger.warn(Log.op(LogOp.MQ_DESTORY_FAIL).kv("msg", ex.getMessage()).toString());
            }
            pushMessageClient = null;
            logger.info(Log.op(LogOp.MQ_PRODUCER_SHUTDOWN).msg("destory done").toString());
        } else {
            logger.info(Log.op(LogOp.MQ_PRODUCER_SHUTDOWN).msg("nothing to destory").toString());
        }
    }

    public void setMqConfig(MQConfig mqConfig) {
        this.mqConfig = mqConfig;
    }

    public void setMqProducerConfig(MQProducerConfig mqProducerConfig) {
        this.mqProducerConfig = mqProducerConfig;
    }

    public void setPushMessageClient(PushMessageClient pushMessageClient) {
        this.pushMessageClient = pushMessageClient;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
