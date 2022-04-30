package cn.xyliang.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import cn.xyliang.mqtt.config.MqttProperties.Config;

/**
 * <p>
 * 描述: 客户端工厂配置回调，用于自定义配置信息
 * </p>
 * 
 * @author aLiang
 * @date 2022年4月30日 下午10:23:56
 */
public interface MqttPahoClientFactorySettingCallback {

	/**
	 * 
	 * <p>
	 * 配置MqttConnectOptions后的回调，用于自定义进行配置，例如可以自定义实现ssl配置
	 * </P>
	 * 
	 * @param options
	 * @param channelName
	 * @param config
	 * @param isConsumer
	 */
	void callback(MqttConnectOptions options, String channelName, Config config, boolean isConsumer);
}
