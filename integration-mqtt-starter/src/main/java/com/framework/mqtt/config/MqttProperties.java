package com.framework.mqtt.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * <p>
 * 描述: mqtt配置文件
 * </p>
 * 
 * @author xingyl
 * @date 2020年3月26日 下午3:52:15
 */
@Data
@ConfigurationProperties(prefix = "mqtt")
@Configuration
public class MqttProperties {

	/**
	 * 本机ip作为clientid的后缀
	 */
	private static String hostAddress;

	private final Map<String, Config> config;

	static {
		try {
			InetAddress address = InetAddress.getLocalHost();
			hostAddress = "_ip_" + address.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Data
	public static class Config {
		/**
		 * 数组tcp://ip:port
		 */
		private String[] url;
		/**
		 * 超时时间，单位：秒
		 */
		private int timeout;
		/**
		 * 心跳时间，秒
		 */
		private int kepAliveInterval;
		/**
		 * qos设置，和topic一一对应
		 */
		private int[] qos;
		/**
		 * 主题，和qos一一对应
		 */
		private String[] topics;
		/**
		 * 账号
		 */
		private String username;
		/**
		 * 密码
		 */
		private String password;
		/**
		 * clientId后是否添加本机ip
		 */
		private Boolean clientIdAppendIp;
		/**
		 * consumer clientId
		 */
		private String consumerClientId;
		/**
		 * producer clientId
		 */
		private String producerClientId;
		/**
		 * 是否异步发送消息
		 */
		private Boolean async;
		/**
		 * consumer遗嘱配置
		 */
		private Will consumerWill;
		/**
		 * producer遗嘱配置
		 */
		private Will producerWill;

		public String getConsumerClientId() {
			return clientIdAppendIp == null || !clientIdAppendIp ? consumerClientId : consumerClientId + hostAddress;
		}

		public String getProducerClientId() {
			return clientIdAppendIp == null || !clientIdAppendIp ? producerClientId : producerClientId + hostAddress;
		}
	}

	@Data
	public static class Will {
		/**
		 * 遗嘱qos设置
		 */
		private int qos;
		/**
		 * 遗嘱主题
		 */
		private String topic;
		/**
		 * 遗嘱内容
		 */
		private String payload;
		/**
		 * 是否发送保留消息
		 */
		private Boolean retained;
	}
}
