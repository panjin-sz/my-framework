/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.servlet.handler;

/**
 * 定义h5收到请求后的下一步的动作
 *
 * @author panjin
 * @version $Id: NextAction.java 2016年7月25日 下午4:08:45 $
 */
public enum NextAction {
    
    /**
     * 跳转
     */
    JUMP("jump"), 
    
    /**
     * 停留在当前页面
     */
    STAY("stay"),
    
    /**
     * 跳转到成功页面
     */
    JUMP_TO_SUC("jumpToSuc"),
    
    /**
     * 跳转到失败页面
     */
    JUMP_TO_FAIL("jumpToFail");
    
    /**
     * 动作
     */
    private String action;
    
    NextAction(String action) {
        this.action = action;
    }
    
    /**
     * 判断action是否存在
     * @param action 动作
     * @return 
     */
    public static boolean isExists(String action) {
        for(NextAction na : NextAction.values()) {
            if(na.action.equals(action)) {
                return true;
            }
        }
        return false;
    }
    
    public String getAction() {
        return action;
    }

    /**
     * 覆盖toString方法，返回action字段
     */
    public String toString() {
        return this.action;
    }
}
