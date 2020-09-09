package com.framework.mqtt.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * 描述: mqtt工具类,可以根据通道名称发送消息
 * </p>
 * 
 * @author xingyl
 * @date 2020年4月1日 上午10:16:35
 */
@Log4j2
public class MqttUtils {

	public static final int QOS_0 = 0;
	public static final int QOS_1 = 1;
	public static final int QOS_2 = 2;

	private final static Map<String, MqttPahoMessageHandler> HANDLER_MAP = new HashMap<>(16);
	public final static String CHANNEL_NAME_SUFFIX = "MqttPahoMessageHandler";

	/**
	 * 存放handler
	 * 
	 * @param channelName
	 * @param handler
	 */
	public static void put(String channelName, MqttPahoMessageHandler handler) {
		HANDLER_MAP.put(channelName + CHANNEL_NAME_SUFFIX, handler);
	}

	/**
	 * 发送消息
	 * 
	 * @param topic       要发送的主题
	 * @param message     消息内容
	 * @param qos         qos级别
	 * @param channelName 发送到指定的通道
	 */
	public static void sendMessage(String topic, String message, int qos, String channelName) {
		MqttPahoMessageHandler handler = getHandler(channelName);
		Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
				.setHeader(MqttHeaders.QOS, qos).build();
		handler.handleMessage(mqttMessage);
	}

	/**
	 * 发送消息,默认qos级别为1
	 * 
	 * @param topic       要发送的主题
	 * @param message     消息内容
	 * @param channelName 发送到指定的通道
	 */
	public static void sendMessage(String topic, String message, String channelName) {
		MqttPahoMessageHandler handler = getHandler(channelName);
		Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
				.setHeader(MqttHeaders.QOS, QOS_1).build();
		handler.handleMessage(mqttMessage);
	}

	/**
	 * 发送消息
	 * 
	 * @param mqttMessage 消息
	 * @param channelName 发送到指定的通道
	 */
	public static void sendMessage(Message<String> mqttMessage, String channelName) {
		MqttPahoMessageHandler handler = getHandler(channelName);
		handler.handleMessage(mqttMessage);
	}

	/**
	 * 如果只有一个通道将使用该通道发送消息
	 * 
	 * @param topic
	 * @param message
	 * @param qos
	 */
	public static void sendMessage(String topic, String message, int qos) {
		MqttPahoMessageHandler handler = getDefaultHeadler();
		Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
				.setHeader(MqttHeaders.QOS, qos).build();
		handler.handleMessage(mqttMessage);
	}

	/**
	 * 如果只有一个通道将使用该通道发送消息，默认qos级别为1
	 * 
	 * @param topic
	 * @param message
	 * @param qos
	 */
	public static void sendMessage(String topic, String message) {
		MqttPahoMessageHandler handler = getDefaultHeadler();
		Message<String> mqttMessage = MessageBuilder.withPayload(message).setHeader(MqttHeaders.TOPIC, topic)
				.setHeader(MqttHeaders.QOS, QOS_1).build();
		handler.handleMessage(mqttMessage);
	}

	/**
	 * 如果只有一个通道将使用该通道发送消息，默认qos级别为1
	 * 
	 * @param mqttMessage 消息信息
	 */
	public static void sendMessage(Message<String> mqttMessage) {
		MqttPahoMessageHandler handler = getDefaultHeadler();
		handler.handleMessage(mqttMessage);
	}

	/**
	 * 获取默认的handler
	 * 
	 * @return
	 */
	private static MqttPahoMessageHandler getDefaultHeadler() {
		Collection<MqttPahoMessageHandler> values = HANDLER_MAP.values();
		Iterator<MqttPahoMessageHandler> iterator = values.iterator();
		MqttPahoMessageHandler handler = iterator.next();
		if (handler == null) {
			log.error("发送消息失败,无可用的headler");
			throw new RuntimeException("发送消息失败,无可用的headler");
		}
		return handler;
	}

	/**
	 * 根据通道获取handler
	 * 
	 * @param channelName
	 * @return
	 */
	private static MqttPahoMessageHandler getHandler(String channelName) {
		MqttPahoMessageHandler handler = HANDLER_MAP.get(channelName + CHANNEL_NAME_SUFFIX);
		if (handler == null) {
			log.error("未查询到相应通道{}的handler，存在的通道名称{}", channelName, HANDLER_MAP.keySet());
			throw new IllegalArgumentException("未查询到相应通道" + channelName + "的handler");
		}
		return handler;
	}
}
