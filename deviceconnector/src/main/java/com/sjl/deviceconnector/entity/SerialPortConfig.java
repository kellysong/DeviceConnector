package com.sjl.deviceconnector.entity;

import java.io.File;

/**
 * 串口配置
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SerialPortConfig
 * @time 2020/11/9 15:59
 * @copyright(C) 2020 song
 */
public class SerialPortConfig {

    private File device;
    private int baudRate;
    private int dataBits;
    private int parity;
    private int stopBits;
    private int flags;

    public SerialPortConfig(Builder builder) {
        this.device = builder.device;
        this.baudRate =  builder.baudRate;
        this.dataBits =  builder.dataBits;
        this.parity =  builder.parity;
        this.stopBits =  builder.stopBits;
        this.flags =  builder.flags;
    }

    public File getDevice() {
        return device;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getParity() {
        return parity;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getFlags() {
        return flags;
    }

    public static Builder newBuilder(int baudrate) {
        return new Builder(baudrate);
    }
    public static Builder newBuilder(File device, int baudrate) {
        return new Builder(device, baudrate);
    }

    public static Builder newBuilder(String devicePath, int baudrate) {
        return new Builder(devicePath, baudrate);
    }

    public final static class Builder {

        private File device;
        private int baudRate;
        private int dataBits = 8;
        private int parity = 0;
        private int stopBits = 1;
        private int flags = 0;

        private Builder(int baudRate) {
            this.baudRate = baudRate;
        }

        private Builder(File device, int baudRate) {
            this.device = device;
            this.baudRate = baudRate;
        }

        private Builder(String devicePath, int baudrate) {
            this(new File(devicePath), baudrate);
        }

        /**
         * 波特率
         *
         * @param baudRate
         * @return
         */
        public Builder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        /**
         * 数据位
         *
         * @param dataBits 默认8,可选值为5~8
         * @return
         */
        public Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        /**
         * 校验位
         *
         * @param parity 0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
         * @return
         */
        public Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        /**
         * 停止位
         *
         * @param stopBits 默认1；1:1位停止位；2:2位停止位
         * @return
         */
        public Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        /**
         * 标志
         *
         * @param flags 默认0
         * @return
         */
        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

   
        public SerialPortConfig build(){
            return new SerialPortConfig(this);
        }
    }
}
