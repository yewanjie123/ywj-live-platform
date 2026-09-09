package com.ywj.live.mq.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 生产者的配置信息
 */
@ConfigurationProperties(prefix="ywj.rmq.producer")
@Configuration
public class RocketMQProducerProperties {

    //rocketmq的nameSever地址
    private String nameSrv;
    //分组名称
    private String groupName;
    //消息重发次数
    private int retryTimes;
    //发送超时时间
    private int sendTimeOut;

    public String getNameSrv() {
        return nameSrv;
    }

    public void setNameSrv(String nameSrv) {
        this.nameSrv = nameSrv;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getSendTimeOut() {
        return sendTimeOut;
    }

    public void setSendTimeOut(int sendTimeOut) {
        this.sendTimeOut = sendTimeOut;
    }

    @Override
    public String toString() {
        return "RocketMQProducerProperties{" +
                "nameSrv='" + nameSrv + '\'' +
                ", groupName='" + groupName + '\'' +
                ", retryTimes=" + retryTimes +
                ", sendTimeOut=" + sendTimeOut +
                '}';
    }
}