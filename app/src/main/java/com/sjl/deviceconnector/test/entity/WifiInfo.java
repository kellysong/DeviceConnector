package com.sjl.deviceconnector.test.entity;

/**
 * Wifi信息
 *
 * @author Kelly
 * @version 1.0.0
 * @filename WifiInfo
 * @time 2022/7/25 17:39
 * @copyright(C) 2022 song
 */
public class WifiInfo {
    private String ip = "127.0.0.1";
    private int port = 8806;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
