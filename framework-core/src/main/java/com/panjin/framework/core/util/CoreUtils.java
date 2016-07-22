/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.util;

/**
 *
 *
 * @author panjin
 * @version $Id: CoreUtils.java 2016年7月22日 下午6:32:41 $
 */
public class CoreUtils {

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
