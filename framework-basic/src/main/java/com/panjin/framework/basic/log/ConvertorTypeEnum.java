/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.basic.log;

/**
 * 加密转换器
 *
 * @author panjin
 * @version $Id: ConvertorTypeEnum.java 2016年7月14日 上午8:47:36 $
 */
public enum ConvertorTypeEnum {

    /**
     * Email类型 ，邮箱转换：jack@gmail.com -> j***@gmail.com
     */
    EMAIL,
    
    /**
     * 手机号码转换: 13798213421 -> 1379****\421
     */
    MOBILE,
    
    /**
     * 电话或手机号码转换, 手机号码同上，电话： (0755)86071510 -> (0755)860***10
     */
    TELEPHONE,
    
    /**
     * 身份证: 11010120141001261X -> 110101*********61X
     */
    IDCARD,
    
    /**
     * 标识转换，匹配value，对email，手机号，身份证进行匹配和转换，其他模式使用普通加密转换器
     */
    IDENTITY,
    
    /**
     * 使用默认转化器转化： 01201410012 -> 0*********2
     */
    DEFAULT,
    
    /**
     * 自定义：可以自定义转换方法，实现ICryptoConvertor接口
     */
    CUSTOME_DEFINE,
    
    /**
     * 10-24位的银行卡号，转换：6226054213548754 -> ************8754
     */
    BANKCARD,
    
    /**
     * 姓名 丁二力-->丁**  欧阳海-->欧**  Rose Zhang--> **** Zhang
     */
    NAME,
    
    /**
     * 个人证件(身份证：包含香港澳门、军官证、警官证、护照、港澳、台湾往来大陆通行证)
     */
    CREDENTIAL,
    
    /**
     * 含有敏感信息字段的对象，包含集合类、数组类型，信息会被转化成json格式 。
     * 敏感信息字段需要用SensitiveBankCard,SensitiveEmail等注解标记
     */
    OBJECT;
}
