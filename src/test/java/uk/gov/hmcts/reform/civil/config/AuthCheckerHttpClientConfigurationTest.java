package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCheckerHttpClientConfigurationTest {

    @Test
    void shouldMarkUserTokenParserHttpClientPrimary_whenDefinitionPresent() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("userTokenParserHttpClient", new RootBeanDefinition(Object.class));
        beanFactory.registerBeanDefinition("serviceTokenParserHttpClient", new RootBeanDefinition(Object.class));

        AuthCheckerHttpClientConfiguration.authCheckerHttpClientPrimary().postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.getBeanDefinition("userTokenParserHttpClient").isPrimary()).isTrue();
        assertThat(beanFactory.getBeanDefinition("serviceTokenParserHttpClient").isPrimary()).isFalse();
    }

    @Test
    void shouldDoNothing_whenUserTokenParserHttpClientDefinitionAbsent() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("serviceTokenParserHttpClient", new RootBeanDefinition(Object.class));

        AuthCheckerHttpClientConfiguration.authCheckerHttpClientPrimary().postProcessBeanFactory(beanFactory);

        assertThat(beanFactory.containsBeanDefinition("userTokenParserHttpClient")).isFalse();
        assertThat(beanFactory.getBeanDefinition("serviceTokenParserHttpClient").isPrimary()).isFalse();
    }
}
