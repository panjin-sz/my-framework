/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.log;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.IdentityHashMap;
import com.alibaba.fastjson.util.TypeUtils;
import com.panjin.framework.basic.log.CryptoConvertConfig.DefaultSensitiveObjectFastJsonConvertor;
import com.panjin.framework.basic.log.sensitive.SensitiveBankCard;
import com.panjin.framework.basic.log.sensitive.SensitiveCredential;
import com.panjin.framework.basic.log.sensitive.SensitiveEmail;
import com.panjin.framework.basic.log.sensitive.SensitiveField;
import com.panjin.framework.basic.log.sensitive.SensitiveIDCard;
import com.panjin.framework.basic.log.sensitive.SensitiveIgnoreOut;
import com.panjin.framework.basic.log.sensitive.SensitiveMobile;
import com.panjin.framework.basic.log.sensitive.SensitiveName;
import com.panjin.framework.basic.util.Pair;

/**
 *
 *
 * @author panjin
 * @version $Id: SensitiveObjectLogUtils.java 2016年7月14日 上午10:13:21 $
 */
public abstract class SensitiveObjectLogUtils {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveObjectLogUtils.class);

    private static final Set<Class<?>> excludeConvert = new HashSet<>();
    static {
        excludeConvert.add(Boolean.class);
        excludeConvert.add(Character.class);
        excludeConvert.add(Byte.class);
        excludeConvert.add(Short.class);
        excludeConvert.add(Integer.class);
        excludeConvert.add(Long.class);
        excludeConvert.add(Float.class);
        excludeConvert.add(Double.class);
        excludeConvert.add(BigDecimal.class);
        excludeConvert.add(BigInteger.class);
        excludeConvert.add(String.class);
        excludeConvert.add(byte[].class);
        excludeConvert.add(short[].class);
        excludeConvert.add(int[].class);
        excludeConvert.add(long[].class);
        excludeConvert.add(float[].class);
        excludeConvert.add(double[].class);
        excludeConvert.add(boolean[].class);
        excludeConvert.add(char[].class);
        excludeConvert.add(Class.class);
        excludeConvert.add(SimpleDateFormat.class);
        excludeConvert.add(Locale.class);
        excludeConvert.add(TimeZone.class);
        excludeConvert.add(UUID.class);
        excludeConvert.add(InetAddress.class);
        excludeConvert.add(Inet4Address.class);
        excludeConvert.add(Inet6Address.class);
        excludeConvert.add(InetSocketAddress.class);
        excludeConvert.add(File.class);
        excludeConvert.add(URI.class);
        excludeConvert.add(URL.class);
        excludeConvert.add(Appendable.class);
        excludeConvert.add(StringBuffer.class);
        excludeConvert.add(StringBuilder.class);
        excludeConvert.add(Pattern.class);
        excludeConvert.add(Charset.class);

        // atomic
        excludeConvert.add(AtomicBoolean.class);
        excludeConvert.add(AtomicInteger.class);
        excludeConvert.add(AtomicLong.class);
        excludeConvert.add(AtomicIntegerArray.class);
        excludeConvert.add(AtomicLongArray.class);

        // awt
        try {
            excludeConvert.add(Class.forName("java.awt.Color"));
            excludeConvert.add(Class.forName("java.awt.Font"));
            excludeConvert.add(Class.forName("java.awt.Point"));
            excludeConvert.add(Class.forName("java.awt.Rectangle"));
        } catch (Throwable e) {
            // skip
        }

    }

    public final static boolean hasSensitiveMsg(Object value) {
        return SensitiveObjectLogConvertorHolder.INSTANCE.contains(value.getClass());
    }

    public final static Object convert(Object value) {
        if (value == null) {
            return String.valueOf(value);
        }

        // 已经有转换器的，直接转换
        DefaultSensitiveObjectFastJsonConvertor convertor = SensitiveObjectLogConvertorHolder.INSTANCE.getConvertor(value);
        if (convertor == null) {
            // 排除类不处理
            Class<?> clazz = value.getClass();
            if (excludeConvert.contains(clazz)) {
                return value;
            }
            scanObjectContainer(value, new IdentityHashMap<>(64));
        }

        return SensitiveObjectLogConvertorHolder.INSTANCE.toJSONString(value);
    }

    /**
     * 扫描集合类、数组类对象
     * 
     * @param value
     * @param referenceMap
     */
    public static void scanObjectContainer(Object value, IdentityHashMap<Object, Object> referenceMap) {
        if (value instanceof Collection<?>) {
            for (Object obj : ((Collection<?>) value)) {
                if (obj != null) {
                    scanObject(obj, referenceMap);
                }
            }
        }
        if (value instanceof Map<?, ?>) {
            Iterator<?> it = ((Map<?, ?>) value).entrySet().iterator();
            Map.Entry<?, ?> entry = null;
            while (it.hasNext()) {
                entry = (Map.Entry<?, ?>) (it.next());
                scanObject(entry.getKey(), referenceMap);
                scanObject(entry.getValue(), referenceMap);
            }
        }
        if (value instanceof Object[]) {
            for (Object obj : ((Object[]) value)) {
                if (obj != null) {
                    scanObject(obj, referenceMap);
                }
            }
        }
    }

    /**
     * 扫描对象
     * 
     * @param value
     * @param referenceMap
     */
    public static void scanObject(Object value, IdentityHashMap<Object, Object> referenceMap) {
        // referenceMap 用来防重扫描
        if (referenceMap.get(value) == null) {
            scanObjectContainer(value, referenceMap);
        }
        if (value.getClass().getName().startsWith("com.ihome")) {
            SensitiveObjectLogConvertorHolder.INSTANCE.scanClassField(value.getClass());
        }
    }

    /**
     * 单例
     * 
     * @author zhengrun
     *
     */
    public static enum SensitiveObjectLogConvertorHolder {
        INSTANCE;
        private SerializeConfig config = new SerializeConfig();
        private Map<String, DefaultSensitiveObjectFastJsonConvertor> classConverterMap = new HashMap<>();
        private WeakHashMap<String, Object> scanedClassSet = new WeakHashMap<>();

        private ReentrantLock lock = new ReentrantLock();

        public SerializeConfig getSerializeConfig() {
            return this.config;
        }

        public boolean contains(Class<?> clazz) {
            if (clazz == null) {
                return false;
            }
            if (scanedClassSet.containsKey(clazz.getName())) {
                return false;
            }
            if (classConverterMap.containsKey(clazz.getName())) {
                return true;
            }

            boolean needSensitiveConvert = scanClass(clazz, null);
            if (!needSensitiveConvert) {
                scanedClassSet.put(clazz.getName(), null);
            }
            return needSensitiveConvert;
        }

        public DefaultSensitiveObjectFastJsonConvertor getConvertor(Object value) {
            if (value == null) {
                return null;
            }
            String clazz = value.getClass().getName();
            if (contains(value.getClass())) {
                return classConverterMap.get(clazz);
            }
            return null;
        }

        /**
         * 扫描类，是否字段有加敏感注解标记
         * 
         * @param clazz
         *            需要扫描敏感注解的类
         * @return
         */
        protected boolean scanClassField(Class<?> clazz) {
            if (clazz == null) {
                return false;
            }
            if (classConverterMap.containsKey(clazz.getName())) {
                return true;
            }
            boolean needSensitiveConvert = false;
            List<FieldInfo> fieldInfoList = TypeUtils.computeGetters(clazz, null, false);
            DefaultSensitiveObjectFastJsonConvertor convertor = new DefaultSensitiveObjectFastJsonConvertor(clazz);
            Pair<Boolean, ICryptoConvertor> fieldConvertor = null;
            for (FieldInfo fieldInfo : fieldInfoList) {
                fieldConvertor = isSensitiveField(fieldInfo.getField());
                if (fieldConvertor.first()) {
                    needSensitiveConvert = true;
                    convertor.add(fieldInfo.getName(), fieldConvertor.second());
                    continue;
                }

                fieldConvertor = isSensitiveField(fieldInfo.getMethod());
                if (fieldConvertor.first()) {
                    needSensitiveConvert = true;
                    convertor.add(fieldInfo.getName(), fieldConvertor.second());
                }

                if (isIgnoreField(fieldInfo)) {
                    convertor.ignoreOutPut(fieldInfo.getName());
                }

                /**
                 * 搜索复杂类是否有需要处理的属性
                 */
                if (fieldInfo.getField() != null && !fieldConvertor.first()) {
                    scanClassField(fieldInfo.getField(), clazz);
                }
            }

            if (needSensitiveConvert) {
                try {
                    lock.lock();
                    classConverterMap.put(clazz.getName(), convertor);
                    config.put(clazz, convertor);
                    // 注册转换器
                    CryptoConvertConfig.registConvertor(clazz.getName(), convertor);
                } finally {
                    lock.unlock();
                }
            }

            return needSensitiveConvert;
        }

        protected boolean scanClass(Class<?> inClass, Class<?> clazz) {
            if (inClass == null || inClass.equals(clazz)) {
                return false;
            }
            if (inClass.getName().startsWith("com.ihome")) {
                return scanClassField(inClass);
            } else {
                if (!excludeConvert.contains(inClass) && !scanedClassSet.containsKey(inClass.getName())) {
                    if (inClass.getTypeParameters() != null && inClass.getTypeParameters().length > 0) {
                        GenericDeclaration gd = null;
                        for (TypeVariable<?> tv : inClass.getTypeParameters()) {
                            gd = tv.getGenericDeclaration();
                            if (gd instanceof Class) {
                                scanClass((Class<?>) gd, inClass);
                            }
                        }
                    }
                }
            }
            return false;
        }

        protected void scanClassField(Field field, Class<?> clazz) {
            Class<?> typeClass = field.getType();

            if (typeClass != null) {
                // 防止循环引用
                if (typeClass.equals(clazz)) {
                    return;
                }
                // 对象类型的属性
                if (typeClass.getName().startsWith("com.ihome")) {
                    scanClass(typeClass, clazz);
                }
                // 数组类型的属性, java中数组类型不可以是参数化类型
                if (Object[].class.isAssignableFrom(typeClass)) {
                    scanClass(typeClass.getComponentType(), clazz);
                }

                // ParameterizedType 参数化类型和参数化集合类型的属性
                if (field.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) (field.getGenericType());
                    for (Type actualType : paramType.getActualTypeArguments()) {
                        if (actualType instanceof Class) {
                            scanClass((Class<?>) actualType, clazz);
                        }
                    }
                }

            }
        }

        public String toJSONString(Object object) {
            SerializeWriter out = new SerializeWriter();
            try {
                JSONSerializer serializer = new JSONSerializer(out, getSerializeConfig());

                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);

                serializer.write(object);

                return out.toString();
            } catch (Exception ex) {
                logger.error(Log.op(SensitiveObjectLogUtils.class.getSimpleName() + ".toJSONString exception").kv("exception", ex.getMessage()).toString(), ex);
                try {
                    return JSON.toJSONString(object);
                } catch (Exception e) {
                    // ^_^ you're unlucky....
                    return String.valueOf(object);
                }
            } finally {
                out.close();
            }
        }

    }

    /**
     * 获取ICryptoConvertor
     * 
     * @param convertorType
     * @param defaultConvertor
     * @return
     */
    public static ICryptoConvertor getConvertor(Class<? extends ICryptoConvertor> convertorType, ICryptoConvertor defaultConvertor) {
        if (convertorType == null) {
            return defaultConvertor;
        }
        Class<? extends ICryptoConvertor> convertor = convertorType;
        ICryptoConvertor icc = defaultConvertor;
        if (defaultConvertor != null && !defaultConvertor.getClass().equals(convertor)) {
            icc = CryptoConvertConfig.getConvertor(convertor.getName());
            if (icc == null) {
                try {
                    icc = convertor.newInstance();

                    CryptoConvertConfig.registConvertor(convertorType.getName(), icc);

                } catch (InstantiationException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return icc;
    }

    public static boolean isIgnoreField(FieldInfo fieldInfo) {
        boolean isIgnore = false;
        if (fieldInfo.getField() != null) {
            SensitiveIgnoreOut ignore = fieldInfo.getField().getAnnotation(SensitiveIgnoreOut.class);
            if (ignore != null) {
                isIgnore = isIgnore | ignore.ignore();
            }
        }
        if (fieldInfo.getMethod() != null) {
            SensitiveIgnoreOut ignore = fieldInfo.getMethod().getAnnotation(SensitiveIgnoreOut.class);
            if (ignore != null) {
                isIgnore = isIgnore | ignore.ignore();
            }
        }
        return isIgnore;
    }

    /**
     * 解析敏感字段注解
     * 
     * @param element
     * @return
     */
    public static Pair<Boolean, ICryptoConvertor> isSensitiveField(AnnotatedElement element) {
        if (element != null) {
            SensitiveField sensitiveField = element.getAnnotation(SensitiveField.class);
            if (sensitiveField != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveField.convertor(), CryptoConvertConfig.getIdentityConvertor()));
            }
            SensitiveBankCard sensitiveBankCard = element.getAnnotation(SensitiveBankCard.class);
            if (sensitiveBankCard != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveBankCard.convertor(), CryptoConvertConfig.getBankCardConvertor()));
            }
            SensitiveEmail sensitiveEmail = element.getAnnotation(SensitiveEmail.class);
            if (sensitiveEmail != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveEmail.convertor(), CryptoConvertConfig.getEmailConvertor()));
            }

            SensitiveIDCard sensitiveIDCard = element.getAnnotation(SensitiveIDCard.class);
            if (sensitiveIDCard != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveIDCard.convertor(), CryptoConvertConfig.getIDCardConvertor()));
            }

            SensitiveMobile sensitiveMobile = element.getAnnotation(SensitiveMobile.class);
            if (sensitiveMobile != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveMobile.convertor(), CryptoConvertConfig.getPhoneConvertor()));
            }

            SensitiveName sensitiveName = element.getAnnotation(SensitiveName.class);
            if (sensitiveName != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveName.convertor(), CryptoConvertConfig.getNameConvertor()));
            }

            SensitiveCredential sensitiveCredential = element.getAnnotation(SensitiveCredential.class);
            if (sensitiveCredential != null) {
                return Pair.of(Boolean.TRUE, getConvertor(sensitiveCredential.convertor(), CryptoConvertConfig.getCredentialConvertor()));
            }

            return Pair.of(Boolean.FALSE, null);
        }
        return Pair.of(Boolean.FALSE, null);
    }

}
