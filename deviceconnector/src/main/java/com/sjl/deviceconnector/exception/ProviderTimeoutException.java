package com.sjl.deviceconnector.exception;

/**
 * 连接提供者超时异常处理
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ProviderTimeoutException
 * @time 2022/7/23 11:27
 * @copyright(C) 2022 song
 */
public class ProviderTimeoutException extends RuntimeException{

    public ProviderTimeoutException() {
    }

    public ProviderTimeoutException(String message) {
        super(message);
    }

    public ProviderTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
