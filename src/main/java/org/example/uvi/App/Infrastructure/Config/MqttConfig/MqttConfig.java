package org.example.uvi.App.Infrastructure.Config.MqttConfig;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.mqtt.core.Mqttv5ClientManager;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaderMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.concurrent.ExecutorService;

@Configuration
@EnableIntegration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id:uvi-server}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.topic.location:location/#}")
    private String locationTopic;

    @Bean
    public MqttConnectionOptions mqttConnectionOptions() {
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[]{brokerUrl});
        if (!username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password.getBytes());
        }
        options.setCleanStart(true);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);
        return options;
    }

    @Bean
    public Mqttv5ClientManager mqttv5ClientManager() {
        return new Mqttv5ClientManager(mqttConnectionOptions(), clientId + "-manager");
    }

    @Bean
    public MessageChannel mqttInboundChannel(
            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor) {
        return new ExecutorChannel(virtualThreadExecutor);
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public Mqttv5PahoMessageDrivenChannelAdapter mqttInboundAdapter(
            @Qualifier("mqttInboundChannel") MessageChannel mqttInboundChannel) {
        Mqttv5PahoMessageDrivenChannelAdapter adapter =
                new Mqttv5PahoMessageDrivenChannelAdapter(mqttv5ClientManager(), locationTopic);
        adapter.setOutputChannel(mqttInboundChannel);
        adapter.setQos(1);
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundHandler() {
        Mqttv5PahoMessageHandler handler =
                new Mqttv5PahoMessageHandler(mqttv5ClientManager());
        handler.setAsync(true);
        handler.setDefaultQos(1);
        handler.setHeaderMapper(new MqttHeaderMapper());
        return handler;
    }
}
