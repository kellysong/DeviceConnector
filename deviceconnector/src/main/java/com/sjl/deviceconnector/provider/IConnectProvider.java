package com.sjl.deviceconnector.provider;

/**
 * 通用连接提供者接口
 *
 * @author Kelly
 */
public interface IConnectProvider {

    /**
     * 打开连接
     *
     * @return 0连接成功，-1连接失败
     */
    int open();


    /**
     * 获取连接状态
     *
     * @return 0连接成功，-1连接失败
     */
    int getState();

    /**
     * 写数据
     *
     * @param sendParams 发送命令
     * @param timeout    超时时间，单位ms
     * @return 0 写成功，-1写失败
     */
    int write(byte[] sendParams, int timeout);


    /**
     * 读数据
     *
     * @param sendParams 发送命令
     * @param buffer     临时缓冲区
     * @param timeout    超时时间，单位ms
     * @return >0读取数据成功（代表数据长度），-1读取超时,-2数据发送错误,-3数据接收错误,更多错误请看{@link com.sjl.deviceconnector.ErrorCode)}
     */
    int read(byte[] sendParams, byte[] buffer, int timeout);


    /**
     * 关闭连接
     */
    void close();







}
