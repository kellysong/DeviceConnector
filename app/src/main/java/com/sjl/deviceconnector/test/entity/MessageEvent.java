package com.sjl.deviceconnector.test.entity;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename MessageEvent
 * @time 2022/6/7 15:26
 * @copyright(C) 2022 song
 */
public class MessageEvent<T> {
    private int code;
    private String flag;
    private T event;

    public MessageEvent(int code, String flag, T event) {
        this.code = code;
        this.flag = flag;
        this.event = event;
    }

    public int getCode() {
        return code;
    }

    public String getFlag() {
        return flag;
    }

    public T getEvent() {
        return event;
    }

    public static class Builder<T> {
        private int code;
        private String flag;
        private T event;


        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setFlag(String flag) {
            this.flag = flag;
            return this;
        }

        public Builder setEvent(T event) {
            this.event = event;
            return this;
        }

        public MessageEvent create() {
            return new MessageEvent<T>(code, flag, event);
        }
    }
}