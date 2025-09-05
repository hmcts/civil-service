package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.assertThat;

class AopConfigTest {

    @Test
    void aopConfig_CanBeInstantiated() {
        // Act
        AopConfig config = new AopConfig();
        // Assert
        assertThat(config).isNotNull();
    }

    @Test
    void aopConfig_HasRequiredAnnotations() {
        // Assert
        assertThat(AopConfig.class.isAnnotationPresent(Configuration.class)).isTrue();
        assertThat(AopConfig.class.isAnnotationPresent(EnableAspectJAutoProxy.class)).isTrue();
        assertThat(AopConfig.class.isAnnotationPresent(ComponentScan.class)).isTrue();

        ComponentScan componentScan = AopConfig.class.getAnnotation(ComponentScan.class);
        assertThat(componentScan.value()).containsExactly("uk.gov.hmcts.reform.civil");
    }

}
