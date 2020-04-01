# mqtt整合

该jar整合了spring-integration-mqtt，只需添加配置，并实现消息订阅接口即可。

### 类说明
* MqttAutoConfiguration为自动配置类
* MqttProperties配置问价映射
* MqttUtils工具类，用来发送mqtt消息

### 配置说明

```yml
mqtt:
  config: 
    channel1:                                          #通道名称，可自定义，订阅消息时需要该名称
      url: [tcp://host1:1883, tcp://host1:1883]        #mqtt的url
      topics: [topic1, topic2]                         #监听的主题，和qos一一对应
      qos: [1, 0]                                      #监听主题的qos，和主题一一对应
      username: admin                                  #用户名
      password: public                                 #密码
      timeout: 60                                      #连接超时时间，单位：秒
      kep-alive-interval: 60                           #心跳时间，单位：秒
      async: true                                      #发送消息时是否异步发送
      client-id-prefix: client_test1_                  #客户端id前缀，会自动生成uuid字符串后缀
    channel2:                                          #通道名称，第二个配置
      url: [tcp://host1:1883, tcp://host1:1883]
      topics: [topic1, topic2]
      qos: [1, 0]
      username: admin
      password: public
      timeout: 60
      kep-alive-interval: 60
      async: true
      client-id-prefix: client_test1_
```

### 订阅消息
订阅消息需要实现MessageHandler接口：

```java
import org.springframework.integration.annotation.ServiceActivator;
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
	
	@ServiceActivator(inputChannel = "channel1")
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		log.info("收到消息---{}", message);
	}

}
```

**配置中每一个channel对应一个MessageHandler实现**

### 发送消息 

MqttUtils工具类中封装了多个发送消息的方法
