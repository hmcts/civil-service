package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PrdAdminUserConfigurationTest {

    private static final String TEST_USERNAME = "prd-admin-user";
    private static final String TEST_PASSWORD = "prd-admin-pass";

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(PrdAdminUserConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasLombokGeneratedMethods() {
        // Assert - verify that @Data generated the expected methods
        assertThat(PrdAdminUserConfiguration.class.getMethods())
            .extracting("name")
            .contains("getUsername", "getPassword", "equals", "hashCode", "toString");
    }

    @Nested
    class ConstructorTests {

        @Test
        void constructor_WithValidParameters_CreatesInstance() {
            // Act
            PrdAdminUserConfiguration config = new PrdAdminUserConfiguration(TEST_USERNAME, TEST_PASSWORD);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(config.getPassword()).isEqualTo(TEST_PASSWORD);
        }

        @Test
        void constructor_WithNullUsername_CreatesInstance() {
            // Act
            PrdAdminUserConfiguration config = new PrdAdminUserConfiguration(null, TEST_PASSWORD);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUsername()).isNull();
            assertThat(config.getPassword()).isEqualTo(TEST_PASSWORD);
        }

        @Test
        void constructor_WithNullPassword_CreatesInstance() {
            // Act
            PrdAdminUserConfiguration config = new PrdAdminUserConfiguration(TEST_USERNAME, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(config.getPassword()).isNull();
        }

        @Test
        void constructor_WithBothNull_CreatesInstance() {
            // Act
            PrdAdminUserConfiguration config = new PrdAdminUserConfiguration(null, null);

            // Assert
            assertThat(config).isNotNull();
            assertThat(config.getUsername()).isNull();
            assertThat(config.getPassword()).isNull();
        }

        @Test
        void constructor_HasValueAnnotations() throws NoSuchMethodException {
            // Act
            Constructor<PrdAdminUserConfiguration> constructor =
                PrdAdminUserConfiguration.class.getConstructor(String.class, String.class);
            Parameter[] parameters = constructor.getParameters();

            // Assert
            assertThat(parameters).hasSize(2);

            // Check first parameter (@Value for username)
            Value usernameValue = parameters[0].getAnnotation(Value.class);
            assertThat(usernameValue).isNotNull();
            assertThat(usernameValue.value()).isEqualTo("${civil.prd-admin.username}");

            // Check second parameter (@Value for password)
            Value passwordValue = parameters[1].getAnnotation(Value.class);
            assertThat(passwordValue).isNotNull();
            assertThat(passwordValue.value()).isEqualTo("${civil.prd-admin.password}");
        }
    }

    @Nested
    class GetterTests {

        private PrdAdminUserConfiguration config;

        @BeforeEach
        void setUp() {
            config = new PrdAdminUserConfiguration(TEST_USERNAME, TEST_PASSWORD);
        }

        @Test
        void getUsername_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getUsername()).isEqualTo(TEST_USERNAME);
        }

        @Test
        void getPassword_ReturnsCorrectValue() {
            // Assert
            assertThat(config.getPassword()).isEqualTo(TEST_PASSWORD);
        }
    }

    @Nested
    class GeneratedMethodTests {

        private PrdAdminUserConfiguration config1;
        private PrdAdminUserConfiguration config2;
        private PrdAdminUserConfiguration config3;

        @BeforeEach
        void setUp() {
            config1 = new PrdAdminUserConfiguration(TEST_USERNAME, TEST_PASSWORD);
            config2 = new PrdAdminUserConfiguration(TEST_USERNAME, TEST_PASSWORD);
            config3 = new PrdAdminUserConfiguration("different", "values");
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
                "PrdAdminUserConfiguration",
                "username=" + TEST_USERNAME,
                "password=" + TEST_PASSWORD
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
            var usernameField = PrdAdminUserConfiguration.class.getDeclaredField("username");
            assertThat(java.lang.reflect.Modifier.isFinal(usernameField.getModifiers())).isTrue();

            var passwordField = PrdAdminUserConfiguration.class.getDeclaredField("password");
            assertThat(java.lang.reflect.Modifier.isFinal(passwordField.getModifiers())).isTrue();
        }

        @Test
        void fields_ArePrivate() throws NoSuchFieldException {
            // Act & Assert
            var usernameField = PrdAdminUserConfiguration.class.getDeclaredField("username");
            assertThat(java.lang.reflect.Modifier.isPrivate(usernameField.getModifiers())).isTrue();

            var passwordField = PrdAdminUserConfiguration.class.getDeclaredField("password");
            assertThat(java.lang.reflect.Modifier.isPrivate(passwordField.getModifiers())).isTrue();
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(PrdAdminUserConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(PrdAdminUserConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(PrdAdminUserConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(PrdAdminUserConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(PrdAdminUserConfiguration.class.getInterfaces()).isEmpty();
    }
}
