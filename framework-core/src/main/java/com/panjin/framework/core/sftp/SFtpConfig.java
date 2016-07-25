/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.sftp;

/**
 * sftp服务器配置
 *
 * @author panjin
 * @version $Id: SFtpConfig.java 2016年7月25日 下午2:25:56 $
 */
public class SFtpConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private int timeout;
    private String privatekey;
    private String privatekeypassphrase;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }

    public String getPrivatekeypassphrase() {
        return privatekeypassphrase;
    }

    public void setPrivatekeypassphrase(String privatekeypassphrase) {
        this.privatekeypassphrase = privatekeypassphrase;
    }

}
