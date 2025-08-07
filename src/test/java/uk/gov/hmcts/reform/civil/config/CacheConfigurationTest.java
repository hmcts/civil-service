package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CacheConfigurationTest {

    private CacheConfiguration cacheConfiguration;

    @BeforeEach
    void setUp() {
        cacheConfiguration = new CacheConfiguration();
    }

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(CacheConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasEnableCachingAnnotation() {
        // Assert
        assertThat(CacheConfiguration.class.isAnnotationPresent(EnableCaching.class)).isTrue();
    }

    @Test
    void cacheManagerCustomizer_HasBeanAnnotation() throws NoSuchMethodException {
        // Act
        Method method = CacheConfiguration.class.getMethod("cacheManagerCustomizer");

        // Assert
        assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
    }

    @Test
    void cacheManagerCustomizer_ReturnsCustomizer() {
        // Act
        CacheManagerCustomizer<CaffeineCacheManager> customizer = cacheConfiguration.cacheManagerCustomizer();

        // Assert
        assertThat(customizer).isNotNull();
    }

    @Test
    void cacheManagerCustomizer_DisablesNullValues() {
        // Arrange
        CaffeineCacheManager mockCacheManager = mock(CaffeineCacheManager.class);
        CacheManagerCustomizer<CaffeineCacheManager> customizer = cacheConfiguration.cacheManagerCustomizer();

        // Act
        customizer.customize(mockCacheManager);

        // Assert
        verify(mockCacheManager).setAllowNullValues(false);
    }

    @Test
    void cacheManagerCustomizer_ReturnsFunctionalCustomizer() {
        // Arrange
        CaffeineCacheManager mockCacheManager1 = mock(CaffeineCacheManager.class);
        CaffeineCacheManager mockCacheManager2 = mock(CaffeineCacheManager.class);
        
        // Act
        CacheManagerCustomizer<CaffeineCacheManager> customizer = cacheConfiguration.cacheManagerCustomizer();
        customizer.customize(mockCacheManager1);
        customizer.customize(mockCacheManager2);

        // Assert - the customizer works on multiple cache managers
        verify(mockCacheManager1).setAllowNullValues(false);
        verify(mockCacheManager2).setAllowNullValues(false);
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(CacheConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(CacheConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(CacheConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void constructor_IsAccessible() throws NoSuchMethodException {
        // Act
        var constructor = CacheConfiguration.class.getDeclaredConstructor();

        // Assert
        assertThat(constructor).isNotNull();
        assertThat(java.lang.reflect.Modifier.isPublic(constructor.getModifiers())).isTrue();
    }

    @Test
    void cacheManagerCustomizer_MethodSignature() throws NoSuchMethodException {
        // Act
        Method method = CacheConfiguration.class.getMethod("cacheManagerCustomizer");

        // Assert
        assertThat(method.getReturnType()).isEqualTo(CacheManagerCustomizer.class);
        assertThat(method.getParameterCount()).isZero();
        assertThat(java.lang.reflect.Modifier.isPublic(method.getModifiers())).isTrue();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(CacheConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(CacheConfiguration.class.getInterfaces()).isEmpty();
    }

    @Test
    void class_HasNoFields() {
        // Assert
        assertThat(CacheConfiguration.class.getDeclaredFields()).isEmpty();
    }
}