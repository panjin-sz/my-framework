/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.form;

import java.util.List;

/**
 * tokenManager的抽象父类
 *
 * @author panjin
 * @version $Id: AbstractTokenManager.java 2016年7月25日 下午5:00:57 $
 */
public abstract class AbstractTokenManager implements TokenManager {

    /**
     * 正确的返回码的list
     */
    List<String> sucCodeList;

    public void setSucCodeList(List<String> sucCodeList) {
        this.sucCodeList = sucCodeList;
    }

    public List<String> getSucCodeList() {
        return sucCodeList;
    }

}
