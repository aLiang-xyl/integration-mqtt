package cn.xyliang.mqtt;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

import cn.xyliang.mqtt.config.MqttProperties.Config;
import cn.xyliang.mqtt.config.MqttProperties.Will;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * 描述: 客户端工厂配置
 * </p>
 * 
 * @author aLiang
 * @date 2022年4月30日 下午10:21:34
 */
@Log4j2
public class MqttPahoClientFactorySetting {

	private MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback;

	public MqttPahoClientFactorySetting(MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback) {
		super();
		this.mqttPahoClientFactorySettingCallback = mqttPahoClientFactorySettingCallback;
	}

	/**
	 * 
	 * <p>
	 * 配置MqttPahoClientFactory
	 * </P>
	 * @param channelName 通道名
	 * @param config      配置信息
	 * @param isConsumer  是否是消费者
	 * @return
	 */
	public MqttPahoClientFactory mqttClientFactory(String channelName, Config config, boolean isConsumer) {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();

		options.setServerURIs(config.getUrl());
		options.setCleanSession(config.getCleanSession() == null ? true : config.getCleanSession());
		options.setKeepAliveInterval(config.getKepAliveInterval());
		options.setPassword(config.getPassword().toCharArray());
		options.setUserName(config.getUsername());
		options.setConnectionTimeout(config.getTimeout());
		options.setAutomaticReconnect(config.getAutomaticReconnect() == null ? true : config.getAutomaticReconnect());

		Integer mqttVersion = config.getMqttVersion();
		if (mqttVersion != null) {
			options.setMqttVersion(mqttVersion);
		}

		Integer maxInflight = config.getMaxInflight();
		if (maxInflight != null) {
			options.setMaxInflight(maxInflight);
		}

		Will will = null;
		if (isConsumer && config.getConsumerWill() != null) {
			will = config.getConsumerWill();
		} else if (!isConsumer && config.getProducerWill() != null) {
			will = config.getProducerWill();
		}
		if (will != null) {
			try {
				options.setWill(will.getTopic(), will.getPayload().getBytes("utf-8"), will.getQos(),
						will.getRetained());
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
			}
		}

		if (mqttPahoClientFactorySettingCallback != null) {
			mqttPahoClientFactorySettingCallback.callback(options, channelName, config, isConsumer);
		}

		factory.setConnectionOptions(options);
		return factory;
	}

	public MqttPahoClientFactorySettingCallback getMqttPahoClientFactorySettingCallback() {
		return mqttPahoClientFactorySettingCallback;
	}

	public void setMqttPahoClientFactorySettingCallback(
			MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback) {
		this.mqttPahoClientFactorySettingCallback = mqttPahoClientFactorySettingCallback;
	}

}
