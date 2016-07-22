/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.mq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.BooleanUtils;

import com.panjin.backend.trace.filters.Trace;
import com.panjin.backend.trace.filters.TraceContext;
import com.panjin.backend.trace.filters.enums.IdTypeEnums;
import com.panjin.backend.trace.filters.support.IdFactory;
import com.panjin.backend.trace.filters.support.Spans;
import com.panjin.backend.trace.filters.support.TraceClientConst;
import com.panjin.backend.trace.meta.model.Annotation;
import com.panjin.backend.trace.meta.model.Span;
import com.panjin.backend.trace.meta.model.TraceType;
import com.panjin.framework.core.dubbo.govern.TraceInitConfig;

/**
 *
 *
 * @author panjin
 * @version $Id: MQTraceUtil.java 2016年7月20日 上午11:29:09 $
 */
public class MQTraceUtil {

    public Trace trace = TraceInitConfig.getTrace();

    protected static final String spanName = "spanName";

    private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();

    protected Span buildSpan(boolean isConsumeSide, String spanName, Map<String, String> attachments) {
        Span span = new Span();
        // 构造span
        if (isConsumeSide) { // 是MQ的消息发送端，但却是被调服务的消费端
            Span parentSpan = null;
            boolean isWeb = trace.getWebContext() != null;
            boolean isService = trace.getServiceContext() != null;
            if (isWeb) {
                parentSpan = trace.getWebContext().getSpan();

            } else if (isService) {
                parentSpan = trace.getServiceContext().getSpan();
            }

            if (parentSpan == null) {
                span = trace.newSpan(spanName);
            } else {
                span = trace.genSpan(parentSpan.getTraceId(), IdFactory.getInstance().getNextId(IdTypeEnums.SPAN_ID.getType(), spanName), parentSpan.getId(),
                        parentSpan.newSubRpcId(), spanName, parentSpan.isSample());
            }

            span.setAppType(trace.getType());
            span.setItemType(TraceType.CALL.getType());

        } else { // MQ consumer
            /** 从消息里获取traceId,parentId,spanId,rpcId **/
            String isSample = attachments.get(TraceClientConst.IS_SAMPLE);
            if (isSample == null) {
                // 外部程序直接调用Dubbo服务
                span = trace.newSpan(spanName);
            } else {
                // 被其他Dubbo服务调用
                String traceId = attachments.get(TraceClientConst.TRACE_ID);
                String parentId = attachments.get(TraceClientConst.PARENT_ID);
                String spanId = attachments.get(TraceClientConst.SPAN_ID);
                String rpcId = attachments.get(TraceClientConst.RPC_ID);
                span = trace.genSpan(traceId, spanId, parentId, rpcId, spanName, BooleanUtils.toBoolean(isSample));
            }

            span.setAppType(trace.getType());
            span.setItemType(TraceType.SERVICE.getType());
        }

        return span;
    }

    /**
     * 获取并发计数器
     */
    protected AtomicInteger getConcurrent(String spanName) {
        AtomicInteger concurrent = concurrents.get(spanName);
        if (concurrent == null) {
            final AtomicInteger atomicInteger = new AtomicInteger();
            concurrent = concurrents.putIfAbsent(spanName, atomicInteger);
            if (concurrent == null) {
                concurrent = atomicInteger;
            }
        }
        return concurrent;
    }

    /**
     * s 调用开始，生成CS|SR Annotation
     * 
     * @param span
     *            span
     * @param consumerSide
     *            是否消费端
     */
    protected void startInvoke(Span span, boolean consumerSide) {
        Long startTime = System.currentTimeMillis();
        if (consumerSide && span.isSample()) {
            // CS Annotation
            Annotation annotation = Spans.genAnnotation(Annotation.AnnType.CS, startTime);
            span.addAnnotation(annotation);
        } else {
            TraceContext traceContext = new TraceContext();
            if (span.isSample()) {
                // SR Annotation
                Annotation annotation = Spans.genAnnotation(Annotation.AnnType.SR, startTime);
                span.addAnnotation(annotation);
            }
            traceContext.setSpan(span);
            trace.setServiceContext(traceContext);
        }
    }

    /**
     * 调用结束，生成CR|SS Annotation
     * 
     * @param consumerSide
     *            是否消费端
     * @param span
     *            span
     */
    protected void endInvoke(Span span, boolean consumerSide) {

        long endTime = System.currentTimeMillis();
        if (consumerSide && span.isSample()) {
            // CR Annotation
            Annotation annotation = Spans.genAnnotation(Annotation.AnnType.CR, endTime);
            span.addAnnotation(annotation);
        } else {
            if (span.isSample()) {
                // SS Annotation
                Annotation annotation = Spans.genAnnotation(Annotation.AnnType.SS, endTime);
                span.addAnnotation(annotation);
            }
            trace.removeServiceContext();
        }
    }
}
