package cn.xyliang.mqtt.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import cn.xyliang.mqtt.MqttPahoClientFactorySetting;
import cn.xyliang.mqtt.MqttPahoClientFactorySettingCallback;
import cn.xyliang.mqtt.config.MqttProperties.Config;
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
	
	@Autowired(required = false)
	private MqttPahoClientFactorySettingCallback mqttPahoClientFactorySettingCallback;

	@ConditionalOnMissingBean(MqttPahoClientFactorySetting.class)
	@Bean
	public MqttPahoClientFactorySetting mqttPahoClientFactorySetting() {
		return new MqttPahoClientFactorySetting(mqttPahoClientFactorySettingCallback);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
		mqttProperties.getConfig().forEach((chnnelName, config) -> init(chnnelName, config));
	}

	/**
	 * 初始化
	 * 
	 * @param channelName
	 * @param config
	 */
	private void init(String channelName, Config config) {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
		// 默认开启consumer
		if (!Boolean.FALSE.equals(config.getConsumerEnable())) {
			// 通道信息
			beanFactory.registerBeanDefinition(channelName, mqttChannel());
			log.info("初始化mqtt, channel {}, 配置 {} ", channelName, config);

			MessageChannel mqttChannel = beanFactory.getBean(channelName, MessageChannel.class);
			beanFactory.registerBeanDefinition(channelName + "MqttChannelAdapter", channelAdapter(channelName, config, mqttChannel));
			log.info("初始化mqtt Channel Adapter");
		}

		// 默认开启consumer
		if (!Boolean.FALSE.equals(config.getProducerEnable())) {
			String handlerBeanName = channelName + MqttUtils.CHANNEL_NAME_SUFFIX;
			beanFactory.registerBeanDefinition(handlerBeanName, mqttOutbound(channelName, config));
			log.info("初始化mqtt MqttPahoMessageHandler");

			MqttUtils.put(channelName, beanFactory.getBean(handlerBeanName, MqttPahoMessageHandler.class));
		}
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
	 * @param channelName
	 * @param config
	 * @param mqttChannel
	 * @return
	 */
	private AbstractBeanDefinition channelAdapter(String channelName, Config config, MessageChannel mqttChannel) {
		BeanDefinitionBuilder messageProducerBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(MqttPahoMessageDrivenChannelAdapter.class);
		messageProducerBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
		messageProducerBuilder.addConstructorArgValue(config.getConsumerClientId());
		messageProducerBuilder
				.addConstructorArgValue(mqttPahoClientFactorySetting().mqttClientFactory(channelName, config, true));
		messageProducerBuilder.addConstructorArgValue(config.getTopics());
		messageProducerBuilder.addPropertyValue("converter", new DefaultPahoMessageConverter());
		messageProducerBuilder.addPropertyValue("qos", config.getQos());
		messageProducerBuilder.addPropertyValue("outputChannel", mqttChannel);

		return messageProducerBuilder.getBeanDefinition();
	}

	/**
	 * 消息发送客户端
	 * 
	 * @param channelName
	 * @param config
	 * @return
	 */
	private AbstractBeanDefinition mqttOutbound(String channelName, Config config) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MqttPahoMessageHandler.class);
		builder.addConstructorArgValue(config.getProducerClientId());
		builder.addConstructorArgValue(mqttPahoClientFactorySetting().mqttClientFactory(channelName, config, false));
		builder.addPropertyValue("async", config.getAsync());

		return builder.getBeanDefinition();
	}
}
