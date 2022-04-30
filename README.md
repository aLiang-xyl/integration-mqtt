# mqtt整合

该jar整合了spring-integration-mqtt，只需添加配置，并实现消息订阅接口即可。

## 依赖
```xml
<dependency>
    <groupId>cn.xyliang</groupId>
    <artifactId>integration-mqtt-starter</artifactId>
    <version>0.0.2</version>
</dependency>
```

### 类说明
* MqttAutoConfiguration为自动配置类
* MqttProperties配置文件映射
* MqttUtils工具类，用来发送mqtt消息

### 配置说明

```yml
mqtt:
  config: 
    channel1:                                          #通道名称，可自定义，订阅消息时需要该名称
      consumer-enable: true                            #是否开启consumer，默认开启
      producer-enable: true                            #是否开启producer，默认开启    
      url: [tcp://host1:1883, tcp://host1:1883]        #mqtt的url
      topics: [topic1, topic2]                         #监听的主题，和qos一一对应
      qos: [1, 0]                                      #监听主题的qos，和主题一一对应
      username: admin                                  #用户名
      password: public                                 #密码
      timeout: 60                                      #连接超时时间，单位：秒
      kep-alive-interval: 60                           #心跳时间，单位：秒
      async: true                                      #发送消息时是否异步发送
      automatic-reconnect: true                        #是否自动重连，不配置的话默认为true
      clean-session: true                              #断开连接时是否清除session，不配置的话默认为true
      max-inflight: 50                                 #max inflight，不配置的话则使用默认值10
      mqtt-version:                                    #mqtt版本，可不配置
      client-id-append-ip: true                        #是否在clientId后面追加本机ip，因为clientid是唯一值，集群环境下不能使用相同的clientid，追加ip可解决该问题
      consumer-client-id: consumer_client_test1        #consumer client id配置
      producer-client-id: producer_client_test1        #producer client id配置
      consumer-will:                                   #consumer遗嘱消息配置
        qos: 1                                         #遗嘱qos
        topic: will_topic                              #遗嘱主题
        payload: '{"id": "consumer_client_test1"}'     #遗嘱内容
        retained: false                                #是否发送保留消息
      producer-will:                                   #producer遗嘱消息配置
        qos: 1                                         #遗嘱qos
        topic: will_topic                              #遗嘱主题
        payload: '{"id": "producer_client_test1"}'     #遗嘱内容
        retained: false                                #是否发送保留消息
    channel2:                                          #通道名称，第二个配置
      url: [tcp://host1:1883, tcp://host1:1883]
      topics: [topic1, topic2]
      qos: [1, 0]
      username: admin
      password: public
      timeout: 60
      kep-alive-interval: 60
      async: true
      automatic-reconnect: true                        
      clean-session: true                              
      max-inflight: 50                                 
      mqtt-version:                                   
      consumer-client-id: consumer_client_test2
      producer-client-id: producer_client_test2
      consumer-will: 
        qos: 1
        topic: will_topic
        payload: '{"id": "consumer_client_test2"}'
        retained: false
      producer-will: 
        qos: 1
        topic: will_topic
        payload: '{"id": "producer_client_test2"}'
        retained: false
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
    
   /**
	* 这里的inputChannel的值和配置中的channel1，channel2，channel3……一一对应
	*/
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

### 系统主题说明

如果想知道运行状态、消息统计、客户端上下线事件，可订阅系统主题。

可参考 [EMQX的系统主题说明](https://docs.emqx.net/broker/latest/cn/advanced/system-topic.html)


# 更新说明

## 2020-07-14 22:51

* 添加遗嘱功能，见配置。

* MqttProperties类中的布尔基础类型改为了封装类型。

## 2020-08-21 11:35

本猿未做过多的测试，springboot2.1.x版本和springboot2.2.x版本bean的加载顺序不一样，导致消息订阅失败。

现已修复了springboot2.1.9版本下订阅消息失败的问题。

## 2020-08-30 14:58

更新了配置方式：
* 去掉了client-id-prefix配置，相应的添加了consumer-client-id，producer-client-id配置，不再自动添加uuid后缀，可添加配置client-id-append-ip决定是否在clientId后追加本机ip

* 去掉了will配置，相应的添加consumer-will,producer-will，分别配置生产者和消费者的遗嘱消息

## 2020-09-11 17:15

* 更改自动配置类路径错误

## 2021-05-25 13:55

* 添加是否开启consumer和producer的开关，具体配置见配置说明

## 2022-04-30 22:54 祝大家五一节玩的开心,愿疫情快快结束

* 添加配置：automatic-reconnect自动连接，clean-session断开是否清除session，max-inflight，mqtt-version，配置方式见上面的配置块文档
* 添加MqttPahoClientFactorySettingCallback，注册为spring的bean则会自动生效，用于配置时的回调，可进行自定义配置MqttConnectOptions，例如配置ssl等