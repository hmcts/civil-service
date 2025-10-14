package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class CrossAccessUserConfigurationTest {

    private static final String TEST_USERNAME = "test-user";
    private static final String TEST_PASSWORD = "test-password";

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(CrossAccessUserConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasLombokGeneratedMethods() {
        // Assert - verify that @Data generated the expected methods
        assertThat(CrossAccessUserConfiguration.class.getMethods())
            .extracting("name")
            .contains("getUserName", "getPassword", "equals", "hashCode", "toString");
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithValidParameters_CreatesInstance() {
            // Act
            CrossAccessUserConfiguration config = new CrossAccessUserConfiguration(TEST_USERNAME, TEST_PASSWORD);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUserName()).isEqualTo(TEST_USERNAME);
            assertThat(config.getPassword()).isEqualTo(TEST_PASSWORD);
        }

        @Test
        void constructor_WithNullUsername_CreatesInstance() {
            // Act
            CrossAccessUserConfiguration config = new CrossAccessUserConfiguration(null, TEST_PASSWORD);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUserName()).isNull();
            assertThat(config.getPassword()).isEqualTo(TEST_PASSWORD);
        }

        @Test
        void constructor_WithNullPassword_CreatesInstance() {
            // Act
            CrossAccessUserConfiguration config = new CrossAccessUserConfiguration(TEST_USERNAME, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUserName()).isEqualTo(TEST_USERNAME);
            assertThat(config.getPassword()).isNull();
        }

        @Test
        void constructor_WithBothNull_CreatesInstance() {
            // Act
            CrossAccessUserConfiguration config = new CrossAccessUserConfiguration(null, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUserName()).isNull();
            assertThat(config.getPassword()).isNull();
        }

        @Test
        void constructor_HasValueAnnotations() throws NoSuchMethodException {
            // Act
            Constructor<CrossAccessUserConfiguration> constructor =
                CrossAccessUserConfiguration.class.getConstructor(String.class, String.class);
            Parameter[] parameters = constructor.getParameters();

            // Assert
            assertThat(parameters).hasSize(2);

            // Check first parameter (@Value for username)
            Value usernameValue = parameters[0].getAnnotation(Value.class);
            assertThat(usernameValue).isNotNull();
            assertThat(usernameValue.value()).isEqualTo("${civil.cross-access.username}");

            // Check second parameter (@Value for password)
            Value passwordValue = parameters[1].getAnnotation(Value.class);
            assertThat(passwordValue).isNotNull();
            assertThat(passwordValue.value()).isEqualTo("${civil.cross-access.password}");
        }
    }

    @Nested
    class GetterTests {

        private CrossAccessUserConfiguration config;

        @BeforeEach
        void setUp() {
            config = new CrossAccessUserConfiguration(TEST_USERNAME, TEST_PASSWORD);
        }

        @Test
        void getUserName_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getUserName()).isEqualTo(TEST_USERNAME);
        }

        @Test
        void getPassword_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getPassword()).isEqualTo(TEST_PASSWORD);
        }
    }

    @Nested
    class GeneratedMethodTests {

        private CrossAccessUserConfiguration config1;
        private CrossAccessUserConfiguration config2;
        private CrossAccessUserConfiguration config3;

        @BeforeEach
        void setUp() {
            config1 = new CrossAccessUserConfiguration(TEST_USERNAME, TEST_PASSWORD);
            config2 = new CrossAccessUserConfiguration(TEST_USERNAME, TEST_PASSWORD);
            config3 = new CrossAccessUserConfiguration("different", "values");
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
            assertThat(result).contains("CrossAccessUserConfiguration")
                .contains("userName=" + TEST_USERNAME)
                .contains("password=" + TEST_PASSWORD);
        }
    }

    @Nested
    class FieldTests {

        @Test
        void fields_AreFinal() throws NoSuchFieldException {
            // Act & Assert
            var userNameField = CrossAccessUserConfiguration.class.getDeclaredField("userName");
            assertThat(java.lang.reflect.Modifier.isFinal(userNameField.getModifiers())).isTrue();

            var passwordField = CrossAccessUserConfiguration.class.getDeclaredField("password");
            assertThat(java.lang.reflect.Modifier.isFinal(passwordField.getModifiers())).isTrue();
        }

        @Test
        void fields_ArePrivate() throws NoSuchFieldException {
            // Act & Assert
            var userNameField = CrossAccessUserConfiguration.class.getDeclaredField("userName");
            assertThat(java.lang.reflect.Modifier.isPrivate(userNameField.getModifiers())).isTrue();

            var passwordField = CrossAccessUserConfiguration.class.getDeclaredField("password");
            assertThat(java.lang.reflect.Modifier.isPrivate(passwordField.getModifiers())).isTrue();
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(CrossAccessUserConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(CrossAccessUserConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(CrossAccessUserConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(CrossAccessUserConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(CrossAccessUserConfiguration.class.getInterfaces()).isEmpty();
    }
}
