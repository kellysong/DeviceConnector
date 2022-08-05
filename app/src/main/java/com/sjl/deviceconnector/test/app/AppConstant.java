package com.sjl.deviceconnector.test.app;



/**
 * 常量
 *
 * @author Kelly
 * @version 1.0.0
 * @filename AppConstant
 * @time 2022/6/7 14:20
 * @copyright(C) 2022 song
 */
public interface AppConstant {

    /**
     * 页面传递参数key
     */
    interface Extras {


    }


    /**
     * 消息事件代码
     */
    interface MessageEventCode {
        int LOG = 1000;

    }


    /**
     * 本地配置的key,都放在这
     */
    interface SpParams {
        String SERIAL_PORT_INFO= "serial_port_info";
        String WIFI_INFO = "wifi_info";

    }


}
