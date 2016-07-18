/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.validator;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;

import com.panjin.framework.basic.exception.BusinessException;

/**
 *
 *
 * @author panjin
 * @version $Id: ValidatorAspect.java 2016年7月18日 下午5:00:35 $
 */
@Aspect
public class ValidatorAspect {

    private ValidatorFactory validatorFactory;
    private ExecutableValidator executableValidator;

    public ValidatorAspect() {
        if (validatorFactory == null) {
            validatorFactory = Validation.byProvider(HibernateValidator.class).configure().failFast(true)
                    .parameterNameProvider(new ParanamerParameterNameProvider()).buildValidatorFactory();
        }
        executableValidator = validatorFactory.getValidator().forExecutables();
    }

    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        // 方法参数校验
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        Object target = pjp.getTarget();
        Object[] args = pjp.getArgs();
        Set<ConstraintViolation<Object>> violations = executableValidator.validateParameters(target, method, args);
        if (!violations.isEmpty()) {
            throw new BusinessException(getViolationMsg(violations));
        }

        Object retVal = pjp.proceed();
        return retVal;
    }

    private String getViolationMsg(Set<ConstraintViolation<Object>> violations) {
        StringBuilder sb = new StringBuilder();
        Iterator<ConstraintViolation<Object>> iter = violations.iterator();
        while (iter.hasNext()) {
            ConstraintViolation<Object> cv = iter.next();
            String argsName = cv.getPropertyPath().toString();
            sb.append(argsName);
            sb.append(cv.getMessage());
            if (iter.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
