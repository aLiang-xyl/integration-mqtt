package com.framework.example.handler;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * <p>描述:配置channel1消息处理 </p>
 * 
 * @author xingyl
 * @date 2020年3月27日 下午6:33:35
 */
@Log4j2
@Component
public class MqttMessageHandler implements MessageHandler {
	
	/**
	 * 自动注入规则：${channelName}MqttChannelAdapter
	 */
	@Autowired
	private MqttPahoMessageDrivenChannelAdapter channel1MqttChannelAdapter;
	
	@PostConstruct
	private void addFailEvent() {
		channel1MqttChannelAdapter.setApplicationEventPublisher((event) -> {
			log.error("失败事件 {}", event);
		});
	}
	
	@ServiceActivator(inputChannel = "channel1")
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		log.info("收到消息---{}", message);
	}

}
