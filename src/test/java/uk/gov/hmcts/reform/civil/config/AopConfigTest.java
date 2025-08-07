package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.assertThat;

class AopConfigTest {

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(AopConfig.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasEnableAspectJAutoProxyAnnotation() {
        // Assert
        assertThat(AopConfig.class.isAnnotationPresent(EnableAspectJAutoProxy.class)).isTrue();
    }

    @Test
    void class_HasComponentScanAnnotation() {
        // Assert
        assertThat(AopConfig.class.isAnnotationPresent(ComponentScan.class)).isTrue();
    }

    @Test
    void componentScan_HasCorrectBasePackage() {
        // Act
        ComponentScan componentScan = AopConfig.class.getAnnotation(ComponentScan.class);

        // Assert
        assertThat(componentScan).isNotNull();
        assertThat(componentScan.value()).containsExactly("uk.gov.hmcts.reform.civil");
        assertThat(componentScan.basePackages()).isEmpty(); // value is used instead
    }

    @Test
    void enableAspectJAutoProxy_HasDefaultSettings() {
        // Act
        EnableAspectJAutoProxy aspectJAutoProxy = AopConfig.class.getAnnotation(EnableAspectJAutoProxy.class);

        // Assert
        assertThat(aspectJAutoProxy).isNotNull();
        assertThat(aspectJAutoProxy.proxyTargetClass()).isFalse(); // default value
        assertThat(aspectJAutoProxy.exposeProxy()).isFalse(); // default value
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(AopConfig.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(AopConfig.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(AopConfig.class.getModifiers())).isFalse();
    }

    @Test
    void constructor_IsAccessible() throws NoSuchMethodException {
        // Act
        var constructor = AopConfig.class.getDeclaredConstructor();

        // Assert
        assertThat(constructor).isNotNull();
        assertThat(java.lang.reflect.Modifier.isPublic(constructor.getModifiers())).isTrue();
    }

    @Test
    void instance_CanBeCreated() {
        // Act & Assert - just verify instantiation works
        AopConfig config = new AopConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(AopConfig.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(AopConfig.class.getInterfaces()).isEmpty();
    }

    @Test
    void class_HasNoFields() {
        // Assert
        assertThat(AopConfig.class.getDeclaredFields()).isEmpty();
    }

    @Test
    void class_HasOnlyDefaultConstructor() {
        // Assert
        assertThat(AopConfig.class.getDeclaredConstructors()).hasSize(1);
    }

    @Test
    void class_HasNoBusinessMethods() {
        // Assert - only inherited methods from Object (ignoring instrumentation)
        long nonInstrumentationMethods = java.util.Arrays.stream(AopConfig.class.getDeclaredMethods())
            .filter(method -> !method.getName().contains("$jacoco"))
            .count();
        assertThat(nonInstrumentationMethods).isZero();
    }
}