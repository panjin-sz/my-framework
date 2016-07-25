/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.web.servlet.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import com.panjin.framework.basic.exception.ApplicationException;
import com.panjin.framework.basic.log.Log;
import com.panjin.framework.basic.log.LogOp;
import com.panjin.framework.basic.util.Pair;
import com.panjin.framework.web.form.FormToken;
import com.panjin.framework.web.form.TokenConst;
import com.panjin.framework.web.form.TokenManager;
import com.panjin.framework.web.servlet.json.JsonMessage;
import com.panjin.framework.web.servlet.json.WebJsonUtil;

/**
 * 基于信息源的异常解析器
 * <p>
 * 指定错误页面、错误码及错误信息，由解析器来进行处理输出
 * 
 * @author panjin
 * @version $Id: MyHandlerExceptionResolver.java 2016年7月25日 下午4:09:55 $
 */
public class MyHandlerExceptionResolver extends AbstractMyHandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(MyHandlerExceptionResolver.class);

    private static String DEFAULT_ERROR_VIEW_NAME = "error";

    private static final String ERROR_CODE_ATTR_NAME = "code";

    private static final String ERROR_MESSAGE_ATTR_NAME = "message";

    private static final String DEFAULT_ERROR_CODE = "500";

    private static final String DEFAULT_ERROR_MESSAGE = "系统错误";

    /**
     * 错误消息内容的分隔符
     */
    public static final String ERR_MSG_SP = "#";

    /**
     * 如果配置的消息格式等于这个的话，默认把异常的原始信息作为错误信息，主要供开发调试的时候用； 配置格式：
     * BindException=${debug} 或
     * org.springframework.validation.BindException=${debug}
     */
    public static final String ERR_DEBUG_MSG = "${debug}";

    /**
     * -----------------------------------------------------
     */

    /**
     * 默认的错误的view名称
     */
    private String errorViewName = DEFAULT_ERROR_VIEW_NAME;

    private String defaultErrorCode = DEFAULT_ERROR_CODE;

    private String defaultErrorMessage = DEFAULT_ERROR_MESSAGE;

    private MessageSource messageSource;

    private TokenManager formTokenManager;

    /**
     * 默认处理所有异常,如果要有不同行为的话,可以在子类扩展
     */
    @Override
    protected boolean support(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        return true;
    }

    /**
     * 记录异常信息
     */
    @Override
    protected void recordException(Exception ex, HttpServletRequest request) {
        logger.error(Log.op(LogOp.EXP_RESOLVER_DEAL).msg("deal url fail").kv("url", request.getRequestURI()).toString(), ex);
    }

    /**
     * 处理非JSON的请求,返回默认的viewName
     */
    @Override
    protected ModelAndView resolveExceptionForNonJson(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView view = new ModelAndView();
        view.setViewName(errorViewName);
        try {
            String errorCode = getErrorCode(request, ex);
            String errorMessage = getErrorMessage(errorCode, request, ex);
            Pair<String, String> pair = parseErrorMessage(errorMessage);

            view.addObject(ERROR_CODE_ATTR_NAME, errorCode);
            view.addObject(ERROR_MESSAGE_ATTR_NAME, pair.second());
            // 生成防重复提交的token
            if (generateToken(handler)) {
                view.addObject(TokenConst.TOKEN, formTokenManager.newToken());
            }
        } catch (Throwable ee) {
            logger.error(Log.op(LogOp.EXP_RESOLVER_FAIL).msg("resolveExceptionForNonJson Fail").toString(), ee);
        }
        return view;
    }

    /**
     * 处理JSON请求
     * 
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @return
     */
    protected ModelAndView resolveExceptionForJson(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            response.setCharacterEncoding(UTF8_ENC);
            response.setContentType(WebJsonUtil.JSON_CONTENT_TYPE);

            JsonMessage jsonMessage = new JsonMessage();
            String errorCode = getErrorCode(request, ex);
            String errorMessage = getErrorMessage(errorCode, request, ex);
            Pair<String, String> pair = parseErrorMessage(errorMessage);
            jsonMessage.setCode(errorCode).setNext(pair.first()).setMessage(pair.second());

            // 生成防重复提交的token
            if (generateToken(handler)) {
                jsonMessage.setToken(formTokenManager.newToken());
            }

            String text = WebJsonUtil.toJSONPString(jsonMessage);
            byte[] bytes = text.getBytes(UTF8);

            return writeOutput(response, text, bytes);
        } catch (Throwable ee) {
            logger.error(Log.op(LogOp.EXP_RESOLVER_FAIL).msg("resolveExceptionForJson Fail").toString(), ee);
            // 暂时返回1个空页面, 如果程序配置了errorPage, 可以做跳转
            return new ModelAndView();
        }
    }

    /**
     * 将错误信息写到response
     * 
     * @param response
     * @param text
     * @param bytes
     * @return
     * @throws IOException
     */
    private ModelAndView writeOutput(HttpServletResponse response, String text, byte[] bytes) throws IOException {
        try {
            // 用OutputStream的方式写入
            OutputStream out = response.getOutputStream();
            out.write(bytes);
            out.flush();
            return new ModelAndView();
        } catch (IllegalStateException ise) {
            // 兼容一下业务方用getWriter()的方式
            String iseMsg = "getWriter() has already been called for this response";
            // if (text.contains("code")) {
            // throw new RuntimeException("xxx", ise);
            // }
            if (iseMsg.equals(ise.getMessage())) {
                Writer writer = response.getWriter();
                writer.write(text);
                writer.flush();
                return new ModelAndView();
            }
        }
        return new ModelAndView();
    }

    /**
     * 获取错误码
     * 
     * @param request
     * @param ex
     * @return
     */
    protected String getErrorCode(HttpServletRequest request, Exception ex) {
        if (ex instanceof ApplicationException) {
            int code = ((ApplicationException) ex).getCode();
            // 如果code为0，则使用默认的错误码
            if (code == 0) {
                return defaultErrorCode;
            } else {
                return String.valueOf(code);
            }
        }
        return defaultErrorCode;
    }

    /**
     * 获取错误消息
     * 
     * @param errorCode
     * @param request
     * @param ex
     * @return
     */
    protected String getErrorMessage(String errorCode, HttpServletRequest request, Exception ex) {
        String retMessage = null;
        if (messageSource != null) {
            retMessage = getErrMsgFromSource(errorCode, ex);
        }
        if (retMessage == null) {
            if (ex instanceof ApplicationException) {
                retMessage = getErrMsgFromSelf(errorCode, ex);
            } else {
                retMessage = defaultErrorMessage;
            }
        }
        return retMessage;
    }

    /**
     * 从异常实例中获取错误信息
     * 
     * @param errorCode
     * @param ex
     * @return
     */
    private String getErrMsgFromSelf(String errorCode, Exception ex) {
        String retMessage;
        if (StringUtils.isNotBlank(ex.getMessage())) {
            retMessage = ex.getMessage();
        } else {
            if (ex.getCause() != null) {
                retMessage = ex.getCause().getMessage();
            } else {
                retMessage = defaultErrorMessage;
                logger.error(Log.op(LogOp.EXP_RESOLVER_FAIL).msg(ex.getClass().getSimpleName() + " no message").kv("code", errorCode).toString(), ex);
            }
        }
        return retMessage;
    }

    /**
     * 用多种不同的方式，从messageSource里面获取错误信息
     * 
     * @param errorCode
     * @param ex
     * @return
     */
    private String getErrMsgFromSource(String errorCode, Exception ex) {
        String retMessage = null;
        Class<?> clz = ex.getClass();
        // 异常是应用类型，支持args，用ExceptionType.code=xx的形式获取配置信息
        if (ex instanceof ApplicationException) {
            retMessage = getErrMsgOfAppException(errorCode, ex, clz);
        }
        // 异常是应用类型或者非应用异常，支持直接通过异常类型获取错误信息，譬如ExceptionType=xxx
        if (retMessage == null) {
            retMessage = getErrMsgOfNonAppException(ex, clz);
        }
        return retMessage;
    }

    /**
     * 从messageSource获取ApplicationException的错误信息
     * 
     * @param errorCode
     * @param ex
     * @param clz
     * @return
     */
    private String getErrMsgOfAppException(String errorCode, Exception ex, Class<?> clz) {
        String retMessage = null;
        String msgKey = null;
        // 应用异常且配置了messageSource
        ApplicationException ae = (ApplicationException) ex;
        msgKey = errorCode;
        // 直接用errorCode去取
        retMessage = getMessageFromSource(msgKey, ae.getArgs());
        // 用"com.ihome.framework.usage.exception.FrameworkUsageException.CustomerException.errorCode"的形式去取
        if (retMessage == null) {
            msgKey = clz.getName() + "." + errorCode;
            retMessage = getMessageFromSource(msgKey, ae.getArgs());
        }
        // 用"CustomerException.errorCode"的形式去取
        if (retMessage == null) {
            msgKey = clz.getSimpleName() + "." + errorCode;
            retMessage = getMessageFromSource(msgKey, ae.getArgs());
        }
        return retMessage;
    }

    /**
     * 从messageSource获取非ApplicationException的错误信息
     * 
     * @param ex
     * @param retMessage
     * @param clz
     * @return
     */
    private String getErrMsgOfNonAppException(Exception ex, Class<?> clz) {
        String retMessage = null;
        String msgKey = null;
        // 下面2种形式，既支持应用异常，也支持非应用异常
        // 用"com.ihome.framework.usage.exception.FrameworkUsageException.CustomerException"的形式去取
        if (retMessage == null) {
            msgKey = clz.getName();
            retMessage = getMessageFromSource(msgKey, null);
            if (ERR_DEBUG_MSG.equals(retMessage)) {
                retMessage = ex.getMessage();
            }
        }
        // 用"CustomerException"的形式去取
        if (retMessage == null) {
            msgKey = clz.getSimpleName();
            retMessage = getMessageFromSource(msgKey, null);
            if (ERR_DEBUG_MSG.equals(retMessage)) {
                retMessage = ex.getMessage();
            }
        }
        return retMessage;
    }

    /**
     * 从messageSource里面获取单个错误信息
     * 
     * @param errorCode
     * @param ae
     * @return
     */
    private String getMessageFromSource(String errorCode, Object[] args) {
        String retMessage = null;
        try {
            // 暂时用中文的Locale，日后有国际化需求的时候，再做修改
            retMessage = messageSource.getMessage(errorCode, args, Locale.SIMPLIFIED_CHINESE);
        } catch (NoSuchMessageException nmex) {
            logger.warn(Log.op(LogOp.EXP_RESOLVER_FAIL).msg("errorCode not in messageSource").kv("code", errorCode).toString());
        }
        return retMessage;
    }

    /**
     * 判断是否需要生成防重复提交的token
     * 
     * @param handler
     * @return
     */
    private boolean generateToken(Object handler) {
        if (formTokenManager == null) {
            return false;
        }
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            Method method = hm.getMethod();
            FormToken formToken = method.getAnnotation(FormToken.class);
            if (formToken == null) {
                return false;
            }
            if (formToken.generateToken()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析错误信息，格式有2种： 1、普通的格式，则直接返回，譬如：“支付密码错误”等
     * 2、特殊的格式，则解析后返回，譬如：“stay#支付密码错误”、"jump#银行方处理失败"
     * 
     * @param errMsg
     * @return Pair, 第1个参数用于赋值给next，第2个参数用于赋值给message
     */
    protected Pair<String, String> parseErrorMessage(String errMsg) {
        if (StringUtils.isBlank(errMsg)) {
            return Pair.of(null, errMsg);
        }
        if (!errMsg.contains(ERR_MSG_SP)) {
            return Pair.of(null, errMsg);
        }
        int index = errMsg.indexOf(ERR_MSG_SP);
        String nextAction = errMsg.substring(0, index);
        String msgRet = errMsg.substring(index + 1, errMsg.length());
        if (NextAction.isExists(nextAction)) {
            return Pair.of(nextAction, msgRet);
        } else {
            // 找不到action的时候，要把原来的错误信息原封不动返回，防止某些错误信息里面确实有#这个字符(by:现金宝，20160226)
            logger.warn(Log.op(LogOp.EXP_NEXT_ACTION_INVALID).msg("nextAction invalid").kv("errMsg", errMsg).kv("nextAction", nextAction).toString());
            return Pair.of(null, errMsg);
        }
    }

    /**
     * @param errorViewName
     *            the errorViewName to set
     */
    public void setErrorViewName(String errorViewName) {
        this.errorViewName = errorViewName;
    }

    /**
     * @param defaultErrorCode
     *            the defaultErrorCode to set
     */
    public void setDefaultErrorCode(String defaultErrorCode) {
        this.defaultErrorCode = defaultErrorCode;
    }

    /**
     * @param defaultErrorMessage
     *            the defaultErrorMessage to set
     */
    public void setDefaultErrorMessage(String defaultErrorMessage) {
        this.defaultErrorMessage = defaultErrorMessage;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public TokenManager getFormTokenManager() {
        return formTokenManager;
    }

    public void setFormTokenManager(TokenManager formTokenManager) {
        this.formTokenManager = formTokenManager;
    }
}
