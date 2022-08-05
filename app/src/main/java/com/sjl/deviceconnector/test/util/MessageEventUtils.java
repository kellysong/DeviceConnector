package com.sjl.deviceconnector.test.util;

import com.sjl.deviceconnector.test.entity.MessageEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename MessageEventUtils
 * @time 2022/7/26 20:29
 * @copyright(C) 2022 song
 */
public class MessageEventUtils {

    public static void sendLog(String log) {
        MessageEvent.Builder<String> stringBuilder = new MessageEvent.Builder<String>();
        stringBuilder.setCode(1000);
        stringBuilder.setEvent(log);
        EventBus.getDefault().post(stringBuilder.create());
    }
}
