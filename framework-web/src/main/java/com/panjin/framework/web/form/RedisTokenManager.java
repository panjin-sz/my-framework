/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author panjin
 * @version $Id: RedisTokenManager.java 2016年7月25日 下午5:05:52 $
 */
public class RedisTokenManager extends AbstractTokenManager {

    private final static Logger logger = LoggerFactory.getLogger(RedisTokenManager.class);
    
    /**
     * 放到REDIS中的token的key的前缀
     */
    private static final String TOKEN_PREFIX = "token_";
    
    /**
     * 空字符串
     */
    private static final String EMPTY_STR = "";

    /**
     * 默认过期时间
     */
    private static final int DEFAULT_EXPIRE = 180;

    /**
     * 插入时候默认的重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 3;

    /**
     * 放到NKV中的token的过期时间
     */
    private int expire = DEFAULT_EXPIRE;
    
    /**
     * token放到REDIS中的重试次数
     */
    private int retryTimes = DEFAULT_RETRY_TIMES;
    
    /** 
     * @see com.panjin.framework.web.form.TokenManager#newToken()
     */
    @Override
    public String newToken() {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see com.panjin.framework.web.form.TokenManager#checkToken(java.lang.String)
     */
    @Override
    public boolean checkToken(String token) {
        // TODO Auto-generated method stub
        return false;
    }

    /** 
     * @see com.panjin.framework.web.form.TokenManager#checkAndDelToken(java.lang.String)
     */
    @Override
    public boolean checkAndDelToken(String token) {
        // TODO Auto-generated method stub
        return false;
    }

}
