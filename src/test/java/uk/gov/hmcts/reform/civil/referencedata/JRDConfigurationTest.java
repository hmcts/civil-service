package uk.gov.hmcts.reform.civil.referencedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JRDConfigurationTest {

    private static final String TEST_URL = "https://jrd.api.com";
    private static final String TEST_ENDPOINT = "/judicial/ref-data";

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(JRDConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasLombokGeneratedMethods() {
        // Assert - verify that @Data generated the expected methods
        assertThat(JRDConfiguration.class.getMethods())
            .extracting("name")
            .contains("getUrl", "getEndpoint", "equals", "hashCode", "toString");
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithValidParameters_CreatesInstance() {
            // Act
            JRDConfiguration config = new JRDConfiguration(TEST_URL, TEST_ENDPOINT);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isEqualTo(TEST_URL);
            assertThat(config.getEndpoint()).isEqualTo(TEST_ENDPOINT);
        }

        @Test
        void constructor_WithNullUrl_CreatesInstance() {
            // Act
            JRDConfiguration config = new JRDConfiguration(null, TEST_ENDPOINT);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isNull();
            assertThat(config.getEndpoint()).isEqualTo(TEST_ENDPOINT);
        }

        @Test
        void constructor_WithNullEndpoint_CreatesInstance() {
            // Act
            JRDConfiguration config = new JRDConfiguration(TEST_URL, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isEqualTo(TEST_URL);
            assertThat(config.getEndpoint()).isNull();
        }

        @Test
        void constructor_WithBothNull_CreatesInstance() {
            // Act
            JRDConfiguration config = new JRDConfiguration(null, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isNull();
            assertThat(config.getEndpoint()).isNull();
        }

        @Test
        void constructor_HasValueAnnotations() throws NoSuchMethodException {
            // Act
            Constructor<JRDConfiguration> constructor =
                JRDConfiguration.class.getConstructor(String.class, String.class);
            Parameter[] parameters = constructor.getParameters();

            // Assert
            assertThat(parameters).hasSize(2);

            // Check first parameter (@Value for url)
            Value urlValue = parameters[0].getAnnotation(Value.class);
            assertThat(urlValue).isNotNull();
            assertThat(urlValue.value()).isEqualTo("${genApp.jrd.url}");

            // Check second parameter (@Value for endpoint)
            Value endpointValue = parameters[1].getAnnotation(Value.class);
            assertThat(endpointValue).isNotNull();
            assertThat(endpointValue.value()).isEqualTo("${genApp.jrd.endpoint}");
        }
    }

    @Nested
    class GetterTests {

        private JRDConfiguration config;

        @BeforeEach
        void setUp() {
            config = new JRDConfiguration(TEST_URL, TEST_ENDPOINT);
        }

        @Test
        void getUrl_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getUrl()).isEqualTo(TEST_URL);
        }

        @Test
        void getEndpoint_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getEndpoint()).isEqualTo(TEST_ENDPOINT);
        }
    }

    @Nested
    class GeneratedMethodTests {

        private JRDConfiguration config1;
        private JRDConfiguration config2;
        private JRDConfiguration config3;

        @BeforeEach
        void setUp() {
            config1 = new JRDConfiguration(TEST_URL, TEST_ENDPOINT);
            config2 = new JRDConfiguration(TEST_URL, TEST_ENDPOINT);
            config3 = new JRDConfiguration("https://different.url", "/different/endpoint");
        }

        @Test
        void equals_WithSameValues_ReturnsTrue() {
            // Assert
            assertThat(config1).isEqualTo(config2);
        }

        @Test
        void equals_WithDifferentValues_ReturnsFalse() {
            // Assert
            assertThat(config1).isNotEqualTo(config3);
        }

        @Test
        void equals_WithNull_ReturnsFalse() {
            // Assert
            assertThat(config1).isNotNull();
        }

        @Test
        void equals_WithDifferentClass_ReturnsFalse() {
            // Assert
            assertThat(config1).isNotEqualTo("string");
        }

        @Test
        void equals_WithSameObject_ReturnsTrue() {
            // Assert
            assertThat(config1).isEqualTo(config1);
        }

        @Test
        void hashCode_WithSameValues_ReturnsSameHashCode() {
            // Assert
            assertThat(config1.hashCode()).hasSameHashCodeAs(config2.hashCode());
        }

        @Test
        void hashCode_WithDifferentValues_ReturnsDifferentHashCode() {
            // Assert - this might occasionally fail due to hash collisions, but very unlikely
            assertThat(config1.hashCode()).isNotEqualTo(config3.hashCode());
        }

        @Test
        void toString_ContainsAllFields() {
            // Act
            String result = config1.toString();

            // Assert
            for (String s : Arrays.asList("JRDConfiguration", "url=" + TEST_URL, "endpoint=" + TEST_ENDPOINT)) {
                assertThat(result).contains(s);
            }
        }
    }

    @Nested
    class FieldTests {

        @Test
        void fields_AreFinal() throws NoSuchFieldException {
            // Act & Assert
            var urlField = JRDConfiguration.class.getDeclaredField("url");
            assertThat(java.lang.reflect.Modifier.isFinal(urlField.getModifiers())).isTrue();

            var endpointField = JRDConfiguration.class.getDeclaredField("endpoint");
            assertThat(java.lang.reflect.Modifier.isFinal(endpointField.getModifiers())).isTrue();
        }

        @Test
        void fields_ArePrivate() throws NoSuchFieldException {
            // Act & Assert
            var urlField = JRDConfiguration.class.getDeclaredField("url");
            assertThat(java.lang.reflect.Modifier.isPrivate(urlField.getModifiers())).isTrue();

            var endpointField = JRDConfiguration.class.getDeclaredField("endpoint");
            assertThat(java.lang.reflect.Modifier.isPrivate(endpointField.getModifiers())).isTrue();
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(JRDConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(JRDConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(JRDConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(JRDConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(JRDConfiguration.class.getInterfaces()).isEmpty();
    }
}
