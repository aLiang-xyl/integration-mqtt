package cn.xyliang.mqtt.config;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import cn.xyliang.mqtt.config.MqttProperties.Config;
import cn.xyliang.mqtt.config.MqttProperties.Will;
import cn.xyliang.mqtt.utils.MqttUtils;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * 描述:mqtt自动配置
 * </p>
 * 
 * @author xingyl
 * @date 2020-03-27 17:08:36
 */
@Log4j2
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttAutoConfiguration implements ApplicationContextAware, BeanPostProcessor {

	private ConfigurableApplicationContext applicationContext;
	@Autowired
	private MqttProperties mqttProperties;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
		mqttProperties.getConfig().forEach((chnnelName, config) -> init(chnnelName, config));
	}

	/**
	 * 初始化
	 */
	private void init(String channelName, Config config) {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
		// 通道信息
		beanFactory.registerBeanDefinition(channelName, mqttChannel());
		log.info("初始化mqtt, channel {}, 配置 {} ", channelName, config);

		MessageChannel mqttChannel = beanFactory.getBean(channelName, MessageChannel.class);
		beanFactory.registerBeanDefinition(channelName + "MqttChannelAdapter", channelAdapter(config, mqttChannel));
		log.info("初始化mqtt Channel Adapter");

		String handlerBeanName = channelName + MqttUtils.CHANNEL_NAME_SUFFIX;
		beanFactory.registerBeanDefinition(handlerBeanName, mqttOutbound(config));
		log.info("初始化mqtt MqttPahoMessageHandler");

		MqttUtils.put(channelName, beanFactory.getBean(handlerBeanName, MqttPahoMessageHandler.class));
	}

	/**
	 * mqtt工厂
	 * 
	 * @param vo
	 * @return
	 */
	private MqttPahoClientFactory mqttClientFactory(Config config, boolean isConsumer) {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();

		options.setServerURIs(config.getUrl());
		options.setCleanSession(true);
		options.setKeepAliveInterval(config.getKepAliveInterval());
		options.setPassword(config.getPassword().toCharArray());
		options.setUserName(config.getUsername());
		options.setConnectionTimeout(config.getTimeout());

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

		factory.setConnectionOptions(options);
		return factory;
	}

	/**
	 * 初始化通道
	 * 
	 * @return
	 */
	private AbstractBeanDefinition mqttChannel() {
		BeanDefinitionBuilder messageChannelBuilder = BeanDefinitionBuilder.genericBeanDefinition(DirectChannel.class);
		messageChannelBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
		return messageChannelBuilder.getBeanDefinition();
	}

	/**
	 * mqtt消息驱动转换器
	 * 
	 * @param vo
	 * @param mqttChannel
	 * @return
	 */
	private AbstractBeanDefinition channelAdapter(Config config, MessageChannel mqttChannel) {
		BeanDefinitionBuilder messageProducerBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(MqttPahoMessageDrivenChannelAdapter.class);
		messageProducerBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
		messageProducerBuilder
				.addConstructorArgValue(config.getConsumerClientId());
		messageProducerBuilder.addConstructorArgValue(mqttClientFactory(config, true));
		messageProducerBuilder.addConstructorArgValue(config.getTopics());
		messageProducerBuilder.addPropertyValue("converter", new DefaultPahoMessageConverter());
		messageProducerBuilder.addPropertyValue("qos", config.getQos());
		messageProducerBuilder.addPropertyValue("outputChannel", mqttChannel);

		return messageProducerBuilder.getBeanDefinition();
	}

	/**
	 * 消息发送客户端
	 * 
	 * @param vo
	 * @return
	 */
	private AbstractBeanDefinition mqttOutbound(Config config) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MqttPahoMessageHandler.class);
		builder.addConstructorArgValue(config.getProducerClientId());
		builder.addConstructorArgValue(mqttClientFactory(config, false));
		builder.addPropertyValue("async", config.getAsync());

		return builder.getBeanDefinition();
	}
}
