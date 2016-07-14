/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.log;

/**
 * 敏感信息转换器
 *
 * @author panjin
 * @version $Id: ICryptoConvertor.java 2016年7月14日 上午9:07:36 $
 */
public interface ICryptoConvertor {
    
    /**
     * 将信息转换成用“*”或其它字符替代的字符串
     * 
     * @param value 原字符串
     * @return 转换后的字符串
     */
    public String convert(Object value);
}
