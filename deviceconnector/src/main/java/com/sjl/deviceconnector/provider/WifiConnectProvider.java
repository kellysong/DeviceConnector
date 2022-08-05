package com.sjl.deviceconnector.provider;



/**
 * Wifi连接提供者
 *
 * @author Kelly
 * @version 1.0.0
 * @filename WifiConnectProvider.java
 * @time 2018/4/13 8:45
 * @copyright(C) 2018 song
 */
public class WifiConnectProvider extends SocketConnectProvider {


    /**
     * 初始化Wifi连接提供者
     *
     * @param host           ip地址
     * @param port           端口号
     * @param connectTimeout 连接超时时间，毫秒
     * @param readTimeout    读取超时时间，毫秒
     */
    public WifiConnectProvider(String host, int port, int connectTimeout, int readTimeout) {
        super(host, port, connectTimeout, readTimeout);
    }
}
