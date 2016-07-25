/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.form;

import java.util.List;

/**
 * 防重Token管理器的接口
 *
 * @author panjin
 * @version $Id: TokenManager.java 2016年7月25日 下午4:37:58 $
 */
public interface TokenManager {

    /**
     * 分配新的token
     * @return
     */
    String newToken();

    /**
     * 检查token是否存在，存在返回true，不存在返回false
     * @param token 
     * @return
     */
    boolean checkToken(String token);
    
    /**
     * 检查token是否存在，如果存在则删除并返回true，不存在则返回false
     * @param token 
     * @return
     */
    boolean checkAndDelToken(String token);
    
    /**
     * 获取正确的返回码的列表
     * @return
     */
    List<String> getSucCodeList();
}
