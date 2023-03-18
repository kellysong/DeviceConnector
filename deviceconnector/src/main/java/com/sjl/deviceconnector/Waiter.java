package com.sjl.deviceconnector;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename Waiter
 * @time 2023/3/4 9:55
 * @copyright(C) 2023 song
 */
public class Waiter {

    public void waitForTimeout(Object toWaitOn, long timeoutMillis) throws InterruptedException {
        toWaitOn.wait(timeoutMillis);
    }

    public void notifyAll(Object toNotify) {
        toNotify.notifyAll();
    }
}