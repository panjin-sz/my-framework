/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.util;

import java.util.regex.Pattern;

/**
 * 正则规则校验工具类
 *
 * @author panjin
 * @version $Id: RegExValidateUtils.java 2016年7月14日 上午10:01:15 $
 */
public final class RegExValidateUtils {

    private static final String EMAIL_PATTERN = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    private static final String TELEPHONE_PATTERN = "^(^0\\d{2}-?\\d{8}$)|(\\([0-9]+\\))?-?[0-9]{7,8}|(^0\\d{3}-?\\d{7}$)|(^0\\d2-?\\d{8}$)|(^0\\d3-?\\d{7}$)$";
    private static final String MOBILE_PATTERN = "^((0|((([+]*)?\\d*)?86|17951)(\\-| )?))?(1[1-9][0-9])[0-9]{8}$";
    private static final String IDCARD_PATTERN = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)";
    private static final String IP_PATTERN = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])"
            + "((\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])){3}|(\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])){5})$";
    private static final String BANK_CARD_PATTERN = "^(\\d{4} ?){2,5}\\d{2,4}$";
    private static final String PASS_PATTERN = "(P\\d{7})|(G\\d{8})";// 护照
    private static final String OFFICER_PATTERN = "[\\u4e00-\\u9fa5a-zA-Z\\d]{1}字第(\\d{8})";// 军官证
    private static final String POLIC_PATTERN = "[\\u4e00-\\u9fa5a-zA-Z\\d]{1}字第(\\d{6}|\\d{7})";// 警官证(有6(江西)，7位<狱警>)
    private static final String HKMC_PATTERN = "(H\\d{10})|(M\\d{10})";// 港澳居民往来内地通行证
    private static final String HKID_PATTERN = "^[a-zA-Z]{1,2}\\d{6}\\([0-9a-zAZ-Z]\\)$";// 香港居民身份证
    private static final String MACAUID_PATTERN = "\\d{7}\\([0-9]\\)";// 澳门居民身份证
    private static final String MTP_PATTERN = "T\\d{8}";// 台湾居民往来大陆通行证

    private static final Pattern IDCARD_REG = Pattern.compile(IDCARD_PATTERN);
    private static final Pattern MOBILE_REG = Pattern.compile(MOBILE_PATTERN);
    private static final Pattern TELEPHONE_REG = Pattern.compile(TELEPHONE_PATTERN);
    private static final Pattern PHONE_REG = Pattern.compile("(" + TELEPHONE_PATTERN + ")|(" + MOBILE_PATTERN + ")");
    private static final Pattern EMAIL_REG = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern IP_REG = Pattern.compile(IP_PATTERN);
    private static final Pattern BANK_CARD_REG = Pattern.compile(BANK_CARD_PATTERN);
    private static final Pattern PASS_REG = Pattern.compile(PASS_PATTERN);
    private static final Pattern OFFICER_REG = Pattern.compile(OFFICER_PATTERN);
    private static final Pattern POLIC_REG = Pattern.compile(POLIC_PATTERN);
    private static final Pattern HKMC_REG = Pattern.compile(HKMC_PATTERN);
    private static final Pattern HKID_REG = Pattern.compile(HKID_PATTERN);
    private static final Pattern MACAUID_REG = Pattern.compile(MACAUID_PATTERN);
    private static final Pattern MTP_REG = Pattern.compile(MTP_PATTERN);

    /**
     * 台湾居民往来大陆通行证
     * 
     * @param code
     * @return
     */
    public static boolean isMTP(String code) {
        if (isEmpty(code)) {
            return false;
        }
        boolean is = MTP_REG.matcher(code).matches();
        return is;
    }

    /**
     * 澳门居民身份证
     * 
     * @param code
     * @return
     */
    public static boolean isMACAUID(String code) {
        if (isEmpty(code)) {
            return false;
        }
        boolean is = MACAUID_REG.matcher(code).matches();
        return is;
    }

    /**
     * 香港居民身份证
     * 
     * @param code
     * @return
     */
    public static boolean isHKID(String code) {
        if (isEmpty(code)) {
            return false;
        }
        boolean is = HKID_REG.matcher(code).matches();
        return is;
    }

    /**
     * 港澳居民往来内地通行证
     * 
     * @param code
     *            港澳居民往来内地通行证号
     * @return
     */
    public static boolean isHKMC(String code) {
        if (isEmpty(code)) {
            return false;
        }
        boolean is = HKMC_REG.matcher(code).matches();
        return is;
    }

    /**
     * 警官证号
     * 
     * @param code
     *            警官证号
     * @return
     */
    public static boolean isPolic(String code) {
        if (isEmpty(code)) {
            return false;
        }
        boolean is = POLIC_REG.matcher(code).matches();
        return is;
    }

    /**
     * 军官证号
     * 
     * @param code
     *            军官证号
     * @return
     */
    public static boolean isOfficer(String code) {
        if (isEmpty(code)) {
            return false;
        }
        boolean is = OFFICER_REG.matcher(code).matches();
        return is;
    }

    /**
     * 护照
     * 
     * @param passcode
     *            护照
     * @return
     */
    public static boolean isPass(String passcode) {
        if (isEmpty(passcode)) {
            return false;
        }
        boolean is = PASS_REG.matcher(passcode).matches();
        return is;
    }

    /**
     * 手机号验证
     * 
     * @param mobile
     *            手机号
     * @return
     */
    public static boolean isMobile(String mobile) {
        if (isEmpty(mobile)) {
            return false;
        }
        boolean isMobile = MOBILE_REG.matcher(mobile).matches();
        return isMobile;
    }

    /**
     * 是否是固定电话
     * 
     * @param telephone
     *            固定电话
     * @return
     */
    public static boolean isTelephone(String telephone) {
        if (isEmpty(telephone)) {
            return false;
        }
        boolean isTelephone = TELEPHONE_REG.matcher(telephone).matches();
        return isTelephone;
    }

    /**
     * 电话验证，包含固话和手机号
     * 
     * @param phone
     *            号码
     * @return
     */
    public static boolean isPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        boolean isPhone = PHONE_REG.matcher(phone).matches();
        return isPhone;
    }

    /**
     * 身份证验证，判断是否15位数字或者17位数字+1位校验码
     * 
     * @param idCard
     *            idCard
     * @return
     */
    public static boolean isIDCard(String idCard) {
        if (isEmpty(idCard)) {
            return false;
        }
        boolean isIdCard = IDCARD_REG.matcher(idCard).matches();
        return isIdCard;
    }

    /**
     * 身份证严格验证，按照国家标准校验身份证，包括格式，第一级地区码，生日有效性，校验码有效性
     * 
     * @param idCard
     *            idCard
     * @return
     */
    public static boolean isIDCardStrict(String idCard) {
        return IDCardValidateUtil.isIdCardValidate(idCard);
    }

    /**
     * 邮箱验证
     * 
     * @param email
     *            email
     * @return
     */
    public static boolean isEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        boolean isEmail = EMAIL_REG.matcher(email).matches();
        return isEmail;
    }

    /**
     * 兼容ipv4和v6的校验
     * 
     * @param ip
     *            ip地址
     * @return
     */
    public static boolean isIPAddress(String ip) {
        if (isEmpty(ip)) {
            return false;
        }
        boolean isIPAddress = IP_REG.matcher(ip).matches();
        return isIPAddress;
    }

    /**
     * 匹配10到24位数字的银行卡，不校验银行卡内容是否有效，只关心位数
     * 
     * @param bankCard
     *            银行卡号
     * @return
     */
    public static boolean isBankCard(String bankCard) {
        if (isEmpty(bankCard)) {
            return false;
        }
        boolean isBankCard = BANK_CARD_REG.matcher(bankCard).matches();
        return isBankCard;
    }

    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }
}
