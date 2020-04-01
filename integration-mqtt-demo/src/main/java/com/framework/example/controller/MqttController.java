package com.framework.example.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.framework.mqtt.config.MqttAutoConfiguration;
import com.framework.mqtt.utils.MqttUtils;

import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xingyl
 * @since 2019-11-22
 */
@Log4j2
@RestController
@AutoConfigureAfter(value = MqttAutoConfiguration.class)
public class MqttController {


	@GetMapping(value = { "/sendMessage" })
	public String sendMessage(String topic) {
		String messageContent = "测试----" + UUID.randomUUID().toString() + "----"
				+ LocalDateTime.now();
		log.info("测试信息 {} ", messageContent);
		MqttUtils.sendMessage(topic, messageContent);
		return messageContent;
	}
	
	@GetMapping(value = { "/sendMessageByChannel" })
	public String sendMessageByChannel(String topic, String channelName) {
		String messageContent = "测试----" + UUID.randomUUID().toString() + "----"
				+ LocalDateTime.now();
		log.info("测试信息 {} ", messageContent);
		MqttUtils.sendMessage(topic, messageContent, channelName);
		return messageContent;
	}
}
