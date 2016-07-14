package com.panjin.framework.basic.log.sensitive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.panjin.framework.basic.log.CryptoConvertConfig.DefaultIdentityCryptoConvertor;
import com.panjin.framework.basic.log.ICryptoConvertor;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface SensitiveField {
    public Class<? extends ICryptoConvertor> convertor() default DefaultIdentityCryptoConvertor.class;
}
