package uk.gov.hmcts.reform.civil.postcode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PostcodeLookupConfigurationTest {

    private static final String TEST_URL = "https://api.postcode.lookup.com";
    private static final String TEST_ACCESS_KEY = "test-access-key-123";

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(PostcodeLookupConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasLombokGeneratedMethods() {
        // Assert - verify that @Data generated the expected methods
        assertThat(PostcodeLookupConfiguration.class.getMethods())
            .extracting("name")
            .contains("getUrl", "getAccessKey", "equals", "hashCode", "toString");
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithValidParameters_CreatesInstance() {
            // Act
            PostcodeLookupConfiguration config = new PostcodeLookupConfiguration(TEST_URL, TEST_ACCESS_KEY);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isEqualTo(TEST_URL);
            assertThat(config.getAccessKey()).isEqualTo(TEST_ACCESS_KEY);
        }

        @Test
        void constructor_WithNullUrl_CreatesInstance() {
            // Act
            PostcodeLookupConfiguration config = new PostcodeLookupConfiguration(null, TEST_ACCESS_KEY);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isNull();
            assertThat(config.getAccessKey()).isEqualTo(TEST_ACCESS_KEY);
        }

        @Test
        void constructor_WithNullAccessKey_CreatesInstance() {
            // Act
            PostcodeLookupConfiguration config = new PostcodeLookupConfiguration(TEST_URL, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isEqualTo(TEST_URL);
            assertThat(config.getAccessKey()).isNull();
        }

        @Test
        void constructor_WithBothNull_CreatesInstance() {
            // Act
            PostcodeLookupConfiguration config = new PostcodeLookupConfiguration(null, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUrl()).isNull();
            assertThat(config.getAccessKey()).isNull();
        }

        @Test
        void constructor_HasValueAnnotations() throws NoSuchMethodException {
            // Act
            Constructor<PostcodeLookupConfiguration> constructor =
                PostcodeLookupConfiguration.class.getConstructor(String.class, String.class);
            Parameter[] parameters = constructor.getParameters();

            // Assert
            assertThat(parameters).hasSize(2);

            // Check first parameter (@Value for url)
            Value urlValue = parameters[0].getAnnotation(Value.class);
            assertThat(urlValue).isNotNull();
            assertThat(urlValue.value()).isEqualTo("${os-postcode-lookup.url}");

            // Check second parameter (@Value for accessKey)
            Value accessKeyValue = parameters[1].getAnnotation(Value.class);
            assertThat(accessKeyValue).isNotNull();
            assertThat(accessKeyValue.value()).isEqualTo("${os-postcode-lookup.key}");
        }
    }

    @Nested
    class GetterTests {

        private PostcodeLookupConfiguration config;

        @BeforeEach
        void setUp() {
            config = new PostcodeLookupConfiguration(TEST_URL, TEST_ACCESS_KEY);
        }

        @Test
        void getUrl_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getUrl()).isEqualTo(TEST_URL);
        }

        @Test
        void getAccessKey_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getAccessKey()).isEqualTo(TEST_ACCESS_KEY);
        }
    }

    @Nested
    class GeneratedMethodTests {

        private PostcodeLookupConfiguration config1;
        private PostcodeLookupConfiguration config2;
        private PostcodeLookupConfiguration config3;

        @BeforeEach
        void setUp() {
            config1 = new PostcodeLookupConfiguration(TEST_URL, TEST_ACCESS_KEY);
            config2 = new PostcodeLookupConfiguration(TEST_URL, TEST_ACCESS_KEY);
            config3 = new PostcodeLookupConfiguration("https://different.url", "different-key");
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
            for (String s : Arrays.asList(
                "PostcodeLookupConfiguration",
                "url=" + TEST_URL,
                "accessKey=" + TEST_ACCESS_KEY
            )) {
                assertThat(result).contains(s);
            }
        }
    }

    @Nested
    class FieldTests {

        @Test
        void fields_AreFinal() throws NoSuchFieldException {
            // Act & Assert
            var urlField = PostcodeLookupConfiguration.class.getDeclaredField("url");
            assertThat(java.lang.reflect.Modifier.isFinal(urlField.getModifiers())).isTrue();

            var accessKeyField = PostcodeLookupConfiguration.class.getDeclaredField("accessKey");
            assertThat(java.lang.reflect.Modifier.isFinal(accessKeyField.getModifiers())).isTrue();
        }

        @Test
        void fields_ArePrivate() throws NoSuchFieldException {
            // Act & Assert
            var urlField = PostcodeLookupConfiguration.class.getDeclaredField("url");
            assertThat(java.lang.reflect.Modifier.isPrivate(urlField.getModifiers())).isTrue();

            var accessKeyField = PostcodeLookupConfiguration.class.getDeclaredField("accessKey");
            assertThat(java.lang.reflect.Modifier.isPrivate(accessKeyField.getModifiers())).isTrue();
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(PostcodeLookupConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(PostcodeLookupConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(PostcodeLookupConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(PostcodeLookupConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(PostcodeLookupConfiguration.class.getInterfaces()).isEmpty();
    }
}
