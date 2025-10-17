package uk.gov.hmcts.reform.civil.sendgrid.config;

import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendGridAutoConfigurationTest {

    private SendGridAutoConfiguration configuration;
    private SendGridProperties properties;

    @BeforeEach
    void setUp() {
        configuration = new SendGridAutoConfiguration();
        properties = mock(SendGridProperties.class);
    }

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(SendGridAutoConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void configuration_HasProxyBeanMethodsFalse() {
        // Act
        Configuration annotation = SendGridAutoConfiguration.class.getAnnotation(Configuration.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.proxyBeanMethods()).isFalse();
    }

    @Test
    void class_HasConditionalOnClassAnnotation() {
        // Assert
        assertThat(SendGridAutoConfiguration.class.isAnnotationPresent(ConditionalOnClass.class)).isTrue();
    }

    @Test
    void conditionalOnClass_HasSendGridClass() {
        // Act
        ConditionalOnClass annotation = SendGridAutoConfiguration.class.getAnnotation(ConditionalOnClass.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly(SendGrid.class);
    }

    @Test
    void class_HasConditionalOnPropertyAnnotation() {
        // Assert
        assertThat(SendGridAutoConfiguration.class.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();
    }

    @Test
    void conditionalOnProperty_HasCorrectSettings() {
        // Act
        ConditionalOnProperty annotation = SendGridAutoConfiguration.class.getAnnotation(ConditionalOnProperty.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.prefix()).isEqualTo("sendgrid");
        assertThat(annotation.value()).containsExactly("api-key");
    }

    @Test
    void class_HasEnableConfigurationPropertiesAnnotation() {
        // Assert
        assertThat(SendGridAutoConfiguration.class.isAnnotationPresent(EnableConfigurationProperties.class)).isTrue();
    }

    @Test
    void enableConfigurationProperties_HasSendGridPropertiesClass() {
        // Act
        EnableConfigurationProperties annotation = SendGridAutoConfiguration.class.getAnnotation(EnableConfigurationProperties.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly(SendGridProperties.class);
    }

    @Test
    void sendGrid_HasBeanAnnotation() throws NoSuchMethodException {
        // Act
        Method method = SendGridAutoConfiguration.class.getMethod("sendGrid", SendGridProperties.class);

        // Assert
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
    }

    @Test
    void sendGrid_HasConditionalOnMissingBeanAnnotation() throws NoSuchMethodException {
        // Act
        Method method = SendGridAutoConfiguration.class.getMethod("sendGrid", SendGridProperties.class);

        // Assert
        assertThat(method.isAnnotationPresent(ConditionalOnMissingBean.class)).isTrue();
    }

    @Test
    void sendGrid_ConditionalOnMissingBean_HasSendGridAPIClass() throws NoSuchMethodException {
        // Act
        Method method = SendGridAutoConfiguration.class.getMethod("sendGrid", SendGridProperties.class);
        ConditionalOnMissingBean annotation = method.getAnnotation(ConditionalOnMissingBean.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly(com.sendgrid.SendGridAPI.class);
    }

    @Nested
    class SendGridBeanCreationTests {

        @Test
        void sendGrid_WithApiKeyOnly_CreatesSendGrid() {
            // Arrange
            when(properties.getApiKey()).thenReturn("test-api-key");
            when(properties.getTest()).thenReturn(null);
            when(properties.getHost()).thenReturn(null);
            when(properties.getVersion()).thenReturn(null);

            // Act
            SendGrid result = configuration.sendGrid(properties);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        void sendGrid_WithTestModeTrue_CreatesSendGridInTestMode() {
            // Arrange
            when(properties.getApiKey()).thenReturn("test-api-key");
            when(properties.getTest()).thenReturn(true);
            when(properties.getHost()).thenReturn(null);
            when(properties.getVersion()).thenReturn(null);

            // Act
            SendGrid result = configuration.sendGrid(properties);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        void sendGrid_WithTestModeFalse_CreatesSendGridInNormalMode() {
            // Arrange
            when(properties.getApiKey()).thenReturn("test-api-key");
            when(properties.getTest()).thenReturn(false);
            when(properties.getHost()).thenReturn(null);
            when(properties.getVersion()).thenReturn(null);

            // Act
            SendGrid result = configuration.sendGrid(properties);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        void sendGrid_WithHost_SetsHost() {
            // Arrange
            String testHost = "https://api.sendgrid.com";
            when(properties.getApiKey()).thenReturn("test-api-key");
            when(properties.getTest()).thenReturn(null);
            when(properties.getHost()).thenReturn(testHost);
            when(properties.getVersion()).thenReturn(null);

            // Act
            SendGrid result = configuration.sendGrid(properties);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getHost()).isEqualTo(testHost);
        }

        @Test
        void sendGrid_WithVersion_SetsVersion() {
            // Arrange
            String testVersion = "v3";
            when(properties.getApiKey()).thenReturn("test-api-key");
            when(properties.getTest()).thenReturn(null);
            when(properties.getHost()).thenReturn(null);
            when(properties.getVersion()).thenReturn(testVersion);

            // Act
            SendGrid result = configuration.sendGrid(properties);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getVersion()).isEqualTo(testVersion);
        }

        @Test
        void sendGrid_WithAllProperties_SetsAllValues() {
            // Arrange
            String testHost = "https://api.sendgrid.com";
            String testVersion = "v3";
            when(properties.getApiKey()).thenReturn("test-api-key");
            when(properties.getTest()).thenReturn(true);
            when(properties.getHost()).thenReturn(testHost);
            when(properties.getVersion()).thenReturn(testVersion);

            // Act
            SendGrid result = configuration.sendGrid(properties);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getHost()).isEqualTo(testHost);
            assertThat(result.getVersion()).isEqualTo(testVersion);
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(SendGridAutoConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(SendGridAutoConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(SendGridAutoConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(SendGridAutoConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(SendGridAutoConfiguration.class.getInterfaces()).isEmpty();
    }
}
