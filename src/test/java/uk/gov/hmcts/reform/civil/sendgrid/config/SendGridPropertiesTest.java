package uk.gov.hmcts.reform.civil.sendgrid.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SendGridPropertiesTest {

    private SendGridProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SendGridProperties();
    }

    @Test
    void class_HasConfigurationPropertiesAnnotation() {
        // Assert
        assertThat(SendGridProperties.class.isAnnotationPresent(ConfigurationProperties.class)).isTrue();
    }

    @Test
    void configurationProperties_HasCorrectPrefix() {
        // Act
        ConfigurationProperties annotation = SendGridProperties.class.getAnnotation(ConfigurationProperties.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("sendgrid");
        assertThat(annotation.prefix()).isEmpty(); // value is used instead
    }

    @Test
    void class_HasLombokGeneratedMethods() {
        // Assert - verify that @Data generated the expected methods
        assertThat(SendGridProperties.class.getMethods())
            .extracting("name")
            .contains("getApiKey", "setApiKey", "getTest", "setTest",
                     "getHost", "setHost", "getVersion", "setVersion",
                     "equals", "hashCode", "toString");
    }

    @Nested
    class GetterSetterTests {

        @Test
        void apiKey_GetterAndSetter_WorkCorrectly() {
            // Arrange
            String apiKey = "SG.test-api-key-123";

            // Act
            properties.setApiKey(apiKey);

            // Assert
            assertThat(properties.getApiKey()).isEqualTo(apiKey);
        }

        @Test
        void test_GetterAndSetter_WorkCorrectly() {
            // Act
            properties.setTest(true);

            // Assert
            assertThat(properties.getTest()).isTrue();

            // Act
            properties.setTest(false);

            // Assert
            assertThat(properties.getTest()).isFalse();
        }

        @Test
        void host_GetterAndSetter_WorkCorrectly() {
            // Arrange
            String host = "smtp.sendgrid.net";

            // Act
            properties.setHost(host);

            // Assert
            assertThat(properties.getHost()).isEqualTo(host);
        }

        @Test
        void version_GetterAndSetter_WorkCorrectly() {
            // Arrange
            String version = "v3";

            // Act
            properties.setVersion(version);

            // Assert
            assertThat(properties.getVersion()).isEqualTo(version);
        }

        @Test
        void allFields_DefaultToNull() {
            // Assert
            assertThat(properties.getApiKey()).isNull();
            assertThat(properties.getTest()).isNull();
            assertThat(properties.getHost()).isNull();
            assertThat(properties.getVersion()).isNull();
        }
    }

    @Nested
    class GeneratedMethodTests {

        private SendGridProperties props1;
        private SendGridProperties props2;
        private SendGridProperties props3;

        @BeforeEach
        void setUp() {
            props1 = new SendGridProperties();
            props1.setApiKey("key1");
            props1.setTest(true);
            props1.setHost("host1");
            props1.setVersion("v1");

            props2 = new SendGridProperties();
            props2.setApiKey("key1");
            props2.setTest(true);
            props2.setHost("host1");
            props2.setVersion("v1");

            props3 = new SendGridProperties();
            props3.setApiKey("key2");
            props3.setTest(false);
            props3.setHost("host2");
            props3.setVersion("v2");
        }

        @Test
        void equals_WithSameValues_ReturnsTrue() {
            // Assert
            assertThat(props1).isEqualTo(props2);
        }

        @Test
        void equals_WithDifferentValues_ReturnsFalse() {
            // Assert
            assertThat(props1).isNotEqualTo(props3);
        }

        @Test
        void equals_WithNull_ReturnsFalse() {
            // Assert
            assertThat(props1).isNotNull();
        }

        @Test
        void equals_WithDifferentClass_ReturnsFalse() {
            // Assert
            assertThat(props1).isNotEqualTo("string");
        }

        @Test
        void equals_WithSameObject_ReturnsTrue() {
            // Assert
            assertThat(props1).isEqualTo(props1);
        }

        @Test
        void hashCode_WithSameValues_ReturnsSameHashCode() {
            // Assert
            assertThat(props1.hashCode()).hasSameHashCodeAs(props2.hashCode());
        }

        @Test
        void hashCode_WithDifferentValues_ReturnsDifferentHashCode() {
            // Assert - this might occasionally fail due to hash collisions, but very unlikely
            assertThat(props1.hashCode()).isNotEqualTo(props3.hashCode());
        }

        @Test
        void toString_ContainsAllFields() {
            // Act
            String result = props1.toString();

            // Assert
            for (String s : Arrays.asList(
                "SendGridProperties",
                "apiKey=key1",
                "test=true",
                "host=host1",
                "version=v1"
            )) {
                assertThat(result).contains(s);
            }
        }
    }

    @Nested
    class ConfigurationBindingTests {

        @Test
        void allFields_CanBeSet() {
            // Arrange
            SendGridProperties props = new SendGridProperties();

            // Act
            props.setApiKey("test-key");
            props.setTest(true);
            props.setHost("test-host");
            props.setVersion("test-version");

            // Assert
            assertThat(props.getApiKey()).isEqualTo("test-key");
            assertThat(props.getTest()).isTrue();
            assertThat(props.getHost()).isEqualTo("test-host");
            assertThat(props.getVersion()).isEqualTo("test-version");
        }

        @Test
        void nullValues_CanBeSet() {
            // Arrange
            SendGridProperties props = new SendGridProperties();
            props.setApiKey("initial");
            props.setTest(true);
            props.setHost("initial");
            props.setVersion("initial");

            // Act
            props.setApiKey(null);
            props.setTest(null);
            props.setHost(null);
            props.setVersion(null);

            // Assert
            assertThat(props.getApiKey()).isNull();
            assertThat(props.getTest()).isNull();
            assertThat(props.getHost()).isNull();
            assertThat(props.getVersion()).isNull();
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(SendGridProperties.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(SendGridProperties.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(SendGridProperties.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(SendGridProperties.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(SendGridProperties.class.getInterfaces()).isEmpty();
    }
}
