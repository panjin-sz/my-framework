/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.log;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.panjin.framework.basic.log.SensitiveObjectLogUtils.SensitiveObjectLogConvertorHolder;
import com.panjin.framework.basic.util.RegExValidateUtils;

/**
 *
 *
 * @author panjin
 * @version $Id: CryptoConvertConfig.java 2016年7月14日 上午9:05:59 $
 */
public class CryptoConvertConfig {

    private static final Map<String, ICryptoConvertor> convertors = new HashMap<String, ICryptoConvertor>();
    private static final Set<String> DEFAULT_KEYS = new HashSet<String>();

    /**
     * email转换key
     */
    public static final String EMAIL_CONVERT = "_email_convert_";

    /**
     * 电话+手机转换key
     */
    public static final String PHONE_CONVERT = "_phone_convert_";

    /**
     * idcard转换key
     */
    public static final String IDCARD_CONVERT = "_idcard_convert_";

    /**
     * 身份标示(手机号，邮箱，用户名，身份证)转换key
     */
    public static final String IDENTITY_CONVERT = "_identity_convert_";

    /**
     * 默认转换key
     */
    public static final String DEFAULT_CONVERT = "_default_convert_";

    /**
     * 银行卡转换key
     */
    public static final String BANK_CARD_CONVERT = "_bank_card_convert_";

    /**
     * 姓名转换key
     */
    public static final String NAME_CONVERT = "_name_convert_";

    /**
     * 银行卡转换key:针对客户
     */
    public static final String BANK_CARDFORCUST_CONVERT = "_bank_cardForCust_convert_";

    /**
     * 银行卡转换key:针对运营
     */
    public static final String BANK_CARDFOROPER_CONVERT = "_bank_cardForOper_convert_";

    /**
     * 个人证件(身份证：包含香港澳门、军官证、警官证、护照、港澳、台湾往来大陆通行证)
     */
    public static final String CREDENTIAL_CONVERT = "_credential_convert_";

    static {
        DEFAULT_KEYS.add(EMAIL_CONVERT);
        DEFAULT_KEYS.add(PHONE_CONVERT);
        DEFAULT_KEYS.add(IDCARD_CONVERT);
        DEFAULT_KEYS.add(IDENTITY_CONVERT);
        DEFAULT_KEYS.add(DEFAULT_CONVERT);
        DEFAULT_KEYS.add(BANK_CARD_CONVERT);
        DEFAULT_KEYS.add(BANK_CARDFORCUST_CONVERT);
        DEFAULT_KEYS.add(BANK_CARDFOROPER_CONVERT);
        DEFAULT_KEYS.add(NAME_CONVERT);
        DEFAULT_KEYS.add(CREDENTIAL_CONVERT);
        registConvertor(EMAIL_CONVERT, new DefaultEmailCryptoConvertor());
        registConvertor(PHONE_CONVERT, new DefaultPhoneCryptoConvertor());
        registConvertor(IDCARD_CONVERT, new DefaultIDCardCryptoConvertor());
        registConvertor(IDENTITY_CONVERT, new DefaultIdentityCryptoConvertor());
        registConvertor(DEFAULT_CONVERT, new DefaultCryptoConvertor());
        registConvertor(BANK_CARD_CONVERT, new DefaultBankCardCryptoConvertor());
        registConvertor(BANK_CARDFORCUST_CONVERT, new BankCardCryptoForCustConvertor());
        registConvertor(BANK_CARDFOROPER_CONVERT, new BankCardCryptoForOperConvertor());
        registConvertor(NAME_CONVERT, new DefaultNameCryptoConvertor());
        registConvertor(CREDENTIAL_CONVERT, new DefaultCredentialCryptoConvertor());
    }

    /**
     * 获取转换实现
     * 
     * @param name
     *            转换实现名
     * @return
     */
    public static ICryptoConvertor getConvertor(String name) {
        ICryptoConvertor convertor = convertors.get(name);
        if (convertor == null && DEFAULT_KEYS.contains(name)) {
            switch (name) {
            case NAME_CONVERT:
                convertor = new DefaultNameCryptoConvertor();
                break;
            case EMAIL_CONVERT:
                convertor = new DefaultEmailCryptoConvertor();
                break;
            case PHONE_CONVERT:
                convertor = new DefaultPhoneCryptoConvertor();
                break;
            case IDCARD_CONVERT:
                convertor = new DefaultIDCardCryptoConvertor();
                break;
            case IDENTITY_CONVERT:
                convertor = new DefaultIdentityCryptoConvertor();
                break;
            case DEFAULT_CONVERT:
                convertor = new DefaultCryptoConvertor();
                break;
            case BANK_CARDFORCUST_CONVERT:
                convertor = new BankCardCryptoForCustConvertor();
                break;
            case BANK_CARDFOROPER_CONVERT:
                convertor = new BankCardCryptoForOperConvertor();
                break;
            case BANK_CARD_CONVERT:
                convertor = new DefaultBankCardCryptoConvertor();
                break;
            case CREDENTIAL_CONVERT:
                convertor = new DefaultCredentialCryptoConvertor();
                break;
            default:
                break;
            }
            if (convertor != null) {
                registConvertor(name, convertor);
            }
        }
        return convertor;
    }

    /**
     * 获取Convertor,如果没有使用默认Convertor
     * 
     * @param name
     *            名称
     * @return
     */
    public static ICryptoConvertor getDefault(String name) {
        ICryptoConvertor convertor = getConvertor(name);
        if (convertor == null) {
            convertor = getConvertor(DEFAULT_CONVERT);
        }
        return convertor;
    }

    public static ICryptoConvertor getEmailConvertor() {
        return getConvertor(EMAIL_CONVERT);
    }

    public static ICryptoConvertor getPhoneConvertor() {
        return getConvertor(PHONE_CONVERT);
    }

    public static ICryptoConvertor getIDCardConvertor() {
        return getConvertor(IDCARD_CONVERT);
    }

    public static ICryptoConvertor getIdentityConvertor() {
        return getConvertor(IDENTITY_CONVERT);
    }

    public static ICryptoConvertor getBankCardConvertor() {
        return getConvertor(BANK_CARD_CONVERT);
    }

    public static ICryptoConvertor getBankCardCryptoForOperConvertor() {
        return getConvertor(BANK_CARDFOROPER_CONVERT);
    }

    public static ICryptoConvertor getBankCardCryptoForCustConvertor() {
        return getConvertor(BANK_CARDFORCUST_CONVERT);
    }

    public static ICryptoConvertor getNameConvertor() {
        return getConvertor(NAME_CONVERT);
    }

    public static ICryptoConvertor getCredentialConvertor() {
        return getConvertor(CREDENTIAL_CONVERT);
    }

    /**
     * 注册convertor
     * 
     * @param name
     *            名称
     * @param convertor
     *            实现
     */
    public static void registConvertor(String name, ICryptoConvertor convertor) {
        requiredParam(name, "registConvertor", "name");
        requiredParam(convertor, "registConvertor", "convertor");
        convertors.put(name, convertor);
    }

    /**
     * 反注册convertor
     * 
     * @param name
     *            名称
     * @return
     */
    public static boolean unregistConvert(String name) {
        if (convertors.containsKey(name)) {
            convertors.remove(name);
            return true;
        } else {
            return false;
        }
    }

    public static class DefaultBankCardCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isBankCard(valueStr)) {
                return convertBankCard(valueStr);
            }
            return connvertCommon(valueStr);
        }
    }

    /**
     * 银行卡的convertor 面向用户展示页面明文显示后四位
     * 
     *
     */
    public static class BankCardCryptoForCustConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isBankCard(valueStr)) {
                return convertBankCardForCustomer(valueStr);
            }
            return connvertCommon(valueStr);
        }
    }

    /**
     * 银行卡的convertor 面向内部（运营、客服等）明文显示前11后4位
     * 
     *
     */
    public static class BankCardCryptoForOperConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isBankCard(valueStr)) {
                return convertBankCardForOper(valueStr);
            }
            return connvertCommon(valueStr);
        }
    }

    /**
     * 身份标示Convertor
     * 
     *
     */
    public static class DefaultIdentityCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isMobile(valueStr)) {
                return convertMobile(valueStr);
            } else if (RegExValidateUtils.isEmail(valueStr)) {
                return convertEmail(valueStr);
            } else if (RegExValidateUtils.isIDCardStrict(valueStr)) {
                return convertIDCard(valueStr);
            } else if (RegExValidateUtils.isBankCard(valueStr)) {
                return convertBankCard(valueStr);
            } else {
                return connvertCommon(valueStr);
            }
        }
    }

    /**
     * IDCard的Convertor
     * 
     *
     */
    public static class DefaultIDCardCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isIDCardStrict(valueStr)) {
                return convertIDCard(valueStr);
            } else {
                return connvertCommon(valueStr);
            }
        }
    }

    /**
     * 个人证件的Convertor
     * 
     *
     */
    public static class DefaultCredentialCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            return convertCredential(valueStr);
        }
    }

    /**
     * Phone的Convertor
     * 
     *
     */
    public static class DefaultPhoneCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isMobile(valueStr)) {
                return convertMobile(valueStr);
            } else if (RegExValidateUtils.isTelephone(valueStr)) {
                return convertTelephone(valueStr);
            } else {
                return connvertCommon(valueStr);
            }
        }

    }

    /**
     * email的convertor
     * 
     *
     */
    public static class DefaultEmailCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            if (RegExValidateUtils.isEmail(valueStr)) {
                return convertEmail(valueStr);
            } else {
                return connvertCommon(valueStr);
            }
        }
    }

    /**
     * 姓名的Convertor
     * 
     *
     */
    public static class DefaultNameCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            String valueStr = String.valueOf(value);
            return convertName(valueStr);
        }
    }

    /**
     * 默认convertor
     * 
     *
     */
    public static class DefaultCryptoConvertor implements ICryptoConvertor {
        @Override
        public String convert(Object value) {
            return connvertCommon(String.valueOf(value));
        }
    }

    /**
     * 默认敏感对象转换类
     * 
     *
     */
    public static class DefaultSensitiveObjectFastJsonConvertor extends JavaBeanSerializer implements ICryptoConvertor, ValueFilter, PropertyPreFilter {
        private Map<String, ICryptoConvertor> convertorMap = new HashMap<>();
        private Set<String> ignoreOutputSet = new HashSet<>();

        private Class<?> clazz;

        public DefaultSensitiveObjectFastJsonConvertor(Class<?> clazz) {
            super(clazz);
            this.clazz = clazz;
        }

        public DefaultSensitiveObjectFastJsonConvertor(Class<?> clazz, String... aliasList) {
            super(clazz, aliasList);
            this.clazz = clazz;
        }

        public DefaultSensitiveObjectFastJsonConvertor(Class<?> clazz, Map<String, String> aliasMap) {
            super(clazz, aliasMap);
            this.clazz = clazz;
        }

        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
            if (!serializer.getValueFilters().contains(this)) {
                serializer.getValueFilters().add(this);
            }
            if (!this.ignoreOutputSet.isEmpty() && !serializer.getPropertyPreFilters().contains(this)) {
                serializer.getPropertyPreFilters().add(this);
            }
            super.write(serializer, object, fieldName, fieldType);
        }

        @Override
        public void writeReference(JSONSerializer serializer, Object object) {
            if (!serializer.getValueFilters().contains(this)) {
                serializer.getValueFilters().add(this);
            }
            if (!this.ignoreOutputSet.isEmpty() && !serializer.getPropertyPreFilters().contains(this)) {
                serializer.getPropertyPreFilters().add(this);
            }
            super.writeReference(serializer, object);
        }

        public void add(String name, ICryptoConvertor convertor) {
            convertorMap.put(name, convertor);
        }

        public void ignoreOutPut(String name) {
            this.ignoreOutputSet.add(name);
        }

        @Override
        public boolean apply(JSONSerializer serializer, Object source, String name) {
            if (source == null) {
                return true;
            }

            if (clazz != null && !clazz.isInstance(source)) {
                return true;
            }

            if (this.ignoreOutputSet.contains(name)) {
                return false;
            }

            return true;
        }

        @Override
        public Object process(Object object, String name, Object value) {
            if (clazz != null && clazz.isInstance(object)) {
                ICryptoConvertor convertor = this.convertorMap.get(name);
                if (convertor != null) {
                    return convertor.convert(value);
                }
            }
            return value;
        }

        @Override
        public String convert(Object value) {
            return SensitiveObjectLogConvertorHolder.INSTANCE.toJSONString(value);
        }
    }

    /**
     * 港澳居民往来内地通行证的convertor H1234567890-->H********90
     * 
     *
     */
    private static String convertMainlandPermitForHKandMacao(String valueStr) {
        int length = valueStr.length();
        return convertCommon(valueStr, 0, length - 2);
    }

    /**
     * 香港\澳门\台湾居民身份证的convertor C123456（1）--> C****56（1）;1234567(8) -->
     * 1****67(8);T12345678-->T****5678;
     * 
     *
     */
    private static String convertIDCardForHKandMacao(String valueStr) {
        return convertCommon(valueStr, 0, 5);
    }

    /**
     * 银行卡的convertor 面向用户展示页面明文显示后四位
     * 
     *
     */
    public static String convertBankCardForCustomer(String valueStr) {
        int length = valueStr.length();
        return convertCommon(valueStr, -1, length - 4);
    }

    /**
     * 银行卡的convertor 面向内部（运营、客服等）明文显示前11后4位
     * 
     *
     */
    public static String convertBankCardForOper(String valueStr) {
        int length = valueStr.length();
        return convertCommon(valueStr, 10, length - 4);
    }

    /**
     * 军官证、警官证、护照的convertor T12345678-->T****5678
     * 
     *
     */
    public static String ConvertOfficerIDAndPass(String valueStr) {
        return convertCommon(valueStr, valueStr.length() - 5, valueStr.length());
    }

    /**
     * 个人证件的convertor 身份证、港澳往来大陆通行证、台湾通行证、香港澳门身份证、军官证、警官证、护照
     * 
     *
     */
    public static String convertCredential(String valueStr) {
        if (RegExValidateUtils.isIDCardStrict(valueStr)) { // 大陆身份证
            return convertIDCard(valueStr);
        } else if (RegExValidateUtils.isHKID(valueStr) || RegExValidateUtils.isMACAUID(valueStr) || RegExValidateUtils.isMTP(valueStr)) {// 香港澳门身份证,台湾通行证
            return convertIDCardForHKandMacao(valueStr);
        } else if (RegExValidateUtils.isHKMC(valueStr)) {// 香港澳门大陆通行证
            return convertMainlandPermitForHKandMacao(valueStr);
        } else if (RegExValidateUtils.isOfficer(valueStr) || RegExValidateUtils.isPolic(valueStr) || RegExValidateUtils.isPass(valueStr)) { // 军官证、警官证、护照
            return ConvertOfficerIDAndPass(valueStr);
        } else {
            return connvertCommon(valueStr);
        }
    }

    private static String convertBankCard(String valueStr) {
        int length = valueStr.length();
        StringBuilder sb = new StringBuilder(length);
        sb.insert(0, valueStr);

        short[] mark = new short[length];
        int fromIndex = 0;
        int count = 0;
        for (int i = 0; i < length; i++) {
            mark[i] = Character.isDigit(sb.charAt(i)) ? (short) 1 : (short) 0;
            if (mark[i] == 1) {
                count++;
                if (count <= 6) {
                    fromIndex = i;
                }
            }
        }

        if (count > 10) {
            int endIndex = 0;
            count = 0;
            for (int i = length - 1; i >= 0; i--) {
                if (mark[i] == 1) {
                    count++;
                    if (count >= 4) {
                        endIndex = i;
                        break;
                    }
                }
            }
            for (int i = fromIndex + 1; i < endIndex; i++) {
                if (mark[i] == 1) {
                    sb.setCharAt(i, '*');
                }
            }
        } else {
            count = 0;
            for (int i = length - 1; i >= 0; i--) {
                if (Character.isDigit(sb.charAt(i))) {
                    if (count < 4) {
                        ++count;
                        continue;
                    } else {
                        sb.setCharAt(i, '*');
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String convertIDCard(String valueStr) {
        // 前留6位，后留4位
        int length = valueStr.length();
        return convertCommon(valueStr, 5, length - 4);
    }

    private static String convertTelephone(String valueStr) {
        int length = valueStr.length();
        return convertCommon(valueStr, length - 6, length - 2);
    }

    private static String convertMobile(String valueStr) {
        int length = valueStr.length();
        return convertCommon(valueStr, 2, length - 4);
    };

    private static String convertEmail(String valueStr) {
        int indexOfAt = valueStr.indexOf('@');
        return convertCommon(valueStr, 0, indexOfAt);
    }

    private static String connvertCommon(String valueStr) {
        if (isEmpty(valueStr)) {
            return valueStr;
        } else {
            if (valueStr.length() == 1) {
                return valueStr;
            }
            if (valueStr.length() == 2) {
                return valueStr.substring(0, 1) + "*";
            }
            StringBuilder sb = new StringBuilder(valueStr.length());
            sb.insert(0, valueStr);
            for (int i = 1; i < valueStr.length() - 1; i++) {
                sb.setCharAt(i, '*');
            }
            return sb.toString();
        }
    }

    /**
     * 姓名的convertor
     * 
     *
     */
    public static String convertName(String valueStr) {
        if (isChinese(valueStr)) {
            // 根据需求，复姓也只显示第一个汉字：张某某，欧阳某某-->张**,欧****
            // 吾布力.买买提--> ****.买买提 :维族姓名格式：前名后姓，之间有’.’间隔，
            // 脱敏规则：明文显示姓氏和’.’，统一用四个’*’指代名
            int sep = valueStr.lastIndexOf(".");
            if (sep > 0) {
                String subs = valueStr.substring(0, sep);
                return valueStr.replaceFirst(subs, "****");
            } else
                return convertCommon(valueStr, 0, valueStr.length());
        } else {
            // 英文姓名：明文显示姓氏，+空格符号，统一用四个’*’指代名
            // Rose Zhang--> **** Zhanglong zhang --->**** zhang
            int sep = valueStr.lastIndexOf(" ");
            if (sep > 0) {
                String subs = valueStr.substring(0, sep);
                return valueStr.replaceFirst(subs, "****");
            } else
                return valueStr;
        }
    }

    public static String convertCommon(String valueStr, int displayPre, int displayEnd) {
        if (isEmpty(valueStr) || displayPre < -1 || displayEnd < 0 || displayEnd < displayPre) {
            return valueStr;
        } else {
            if (valueStr.length() == 1) {
                return valueStr;
            }
            if (valueStr.length() <= displayPre) {
                return valueStr;
            }
            if (valueStr.length() <= displayEnd) {
                displayEnd = valueStr.length();
            }

            StringBuilder sb = new StringBuilder(valueStr.length());
            sb.insert(0, valueStr);
            for (int i = displayPre + 1; i < displayEnd; i++) {
                sb.setCharAt(i, '*');
            }
            return sb.toString();
        }
    }

    // 根据Unicode编码判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        String s = String.valueOf(value).trim();
        if ("".equals(s) || "null".equalsIgnoreCase(s)) {
            return true;
        }
        return false;
    }

    private static void requiredParam(Object param, String method, String parameter) {
        if (null == param) {
            throw new IllegalArgumentException("Required parameter: {param: " + parameter + ", method: " + method + "}");
        }
    }
}
