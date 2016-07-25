/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.form;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防表单重复提交的token
 *
 * @author panjin
 * @version $Id: FormToken.java 2016年7月25日 下午4:15:51 $
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormToken {

    // 生成token
    boolean generateToken() default false;

    // 检验token
    boolean checkToken() default false;
}
