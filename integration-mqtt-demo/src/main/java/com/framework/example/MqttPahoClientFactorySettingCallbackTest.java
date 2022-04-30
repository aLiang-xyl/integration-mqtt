package com.framework.example;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.stereotype.Component;

import cn.xyliang.mqtt.MqttPahoClientFactorySettingCallback;
import cn.xyliang.mqtt.config.MqttProperties.Config;
import lombok.extern.log4j.Log4j2;

/**
 * <p>描述: </p>
 * 
 * @author aLiang
 * @date 2022年4月30日 下午10:45:16
 */
@Log4j2
@Component
public class MqttPahoClientFactorySettingCallbackTest implements MqttPahoClientFactorySettingCallback {

	@Override
	public void callback(MqttConnectOptions options, String channelName, Config config, boolean isConsumer) {
		log.info("这是自定义配置，可以用来配置ssl相关，channelName:{}, isConsumer:{}", channelName, isConsumer);
	}

}
