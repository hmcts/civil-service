package uk.gov.hmcts.reform.civil.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@Slf4j
@Configuration
public class HearingsJmsConfig {

    public static final String AMQP_CONNECTION_STRING_TEMPLATE = "amqps://%1s";

    @Value("${azure.service-bus.hmc-to-hearings-api.namespace}")
    private String namespace;

    @Value("${azure.service-bus.connection-postfix}")
    private String connectionPostfix;

    @Value("${azure.service-bus.hmc-to-hearings-api.username}")
    private String username;

    @Value("${azure.service-bus.hmc-to-hearings-api.password}")
    private String password;

    @Value("${azure.service-bus.hmc-to-hearings-api.receiveTimeout}")
    private Long receiveTimeout;

    @Value("${azure.service-bus.hmc-to-hearings-api.idleTimeout}")
    private Long idleTimeout;

    @Value("${azure.service-bus.hmc-to-hearings-api.topicName}")
    private String topicName;

    @Value("${azure.service-bus.hmc-to-hearings-api.subscriptionName}")
    private String subscriptionName;

    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> hmcHearingsEventTopicContainerFactory(
        ConnectionFactory hmcHearingJmsConnectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(hmcHearingJmsConnectionFactory);
        factory.setReceiveTimeout(receiveTimeout);
        factory.setSubscriptionDurable(Boolean.TRUE);
        factory.setSessionTransacted(Boolean.TRUE);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);

        configurer.configure(factory, hmcHearingJmsConnectionFactory);
        return factory;
    }

    @Bean
    public ConnectionFactory hmcHearingJmsConnectionFactory(@Value("${spring.application.name}") final String clientId) {
        log.info("idleTimeout {}", idleTimeout);
        log.info("receiveTimeout {}", receiveTimeout);
        String connection = String.format(AMQP_CONNECTION_STRING_TEMPLATE, namespace + connectionPostfix);
        log.info("connection String {}", connection);
//        connection = String.format(AMQP_CONNECTION_STRING_TEMPLATE, namespace + connectionPostfix, 3600000L);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(connection);
        jmsConnectionFactory.setUsername(username);
        jmsConnectionFactory.setPassword(password);
        jmsConnectionFactory.setClientID(clientId);

        return new CachingConnectionFactory(jmsConnectionFactory);
    }
}
