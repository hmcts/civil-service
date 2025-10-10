package uk.gov.hmcts.reform.civil.bankholidays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class BankHolidaysApiTest {

    private Method retrieveAllMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        retrieveAllMethod = BankHolidaysApi.class.getMethod("retrieveAll");
    }

    @Test
    void interface_HasFeignClientAnnotation() {
        // Assert
        assertThat(BankHolidaysApi.class.isAnnotationPresent(FeignClient.class)).isTrue();
    }

    @Test
    void feignClient_HasCorrectConfiguration() {
        // Act
        FeignClient annotation = BankHolidaysApi.class.getAnnotation(FeignClient.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("bank-holidays-api");
        assertThat(annotation.url()).isEqualTo("${bankHolidays.api.url}");
        assertThat(annotation.configuration()).isEmpty();
    }

    @Test
    void interface_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(BankHolidaysApi.class.getModifiers())).isTrue();
    }

    @Test
    void interface_IsInterface() {
        // Assert
        assertThat(BankHolidaysApi.class.isInterface()).isTrue();
    }

    @Test
    void retrieveAll_HasGetMappingAnnotation() {
        // Assert
        assertThat(retrieveAllMethod.isAnnotationPresent(GetMapping.class)).isTrue();
    }

    @Test
    void retrieveAll_GetMappingConfiguration() {
        // Act
        GetMapping annotation = retrieveAllMethod.getAnnotation(GetMapping.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.path()).containsExactly("/bank-holidays.json");
        assertThat(annotation.value()).isEmpty();
    }

    @Test
    void retrieveAll_ReturnsCorrectType() {
        // Assert
        assertThat(retrieveAllMethod.getReturnType()).isEqualTo(BankHolidays.class);
    }

    @Test
    void retrieveAll_HasNoParameters() {
        // Act
        Parameter[] parameters = retrieveAllMethod.getParameters();

        // Assert
        assertThat(parameters).isEmpty();
    }

    @Test
    void retrieveAll_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(retrieveAllMethod.getModifiers())).isTrue();
    }

    @Test
    void retrieveAll_IsAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(retrieveAllMethod.getModifiers())).isTrue();
    }

    @Test
    void interface_HasOneMethod() {
        // Act
        Method[] methods = BankHolidaysApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(1);
        assertThat(methods[0].getName()).isEqualTo("retrieveAll");
    }

    @Test
    void interface_ExtendsNoInterfaces() {
        // Assert
        assertThat(BankHolidaysApi.class.getInterfaces()).isEmpty();
    }

    @Test
    void interface_HasCorrectPackage() {
        // Assert
        assertThat(BankHolidaysApi.class.getPackage().getName()).isEqualTo("uk.gov.hmcts.reform.civil.bankholidays");
    }

    @Test
    void retrieveAll_HasNoAnnotationBesidesGetMapping() {
        // Assert - verify it doesn't have extra annotations like RequestBody, ResponseBody, etc.
        assertThat(retrieveAllMethod.getAnnotations()).hasSize(1);
        assertThat(retrieveAllMethod.isAnnotationPresent(GetMapping.class)).isTrue();
    }

    @Test
    void retrieveAll_ThrowsNoExceptions() {
        // Assert
        assertThat(retrieveAllMethod.getExceptionTypes()).isEmpty();
    }

    @Test
    void interface_IsMinimalApi() {
        // This test verifies this is a simple, minimal API with just one method
        assertThat(BankHolidaysApi.class.getDeclaredFields()).isEmpty();
        assertThat(BankHolidaysApi.class.getDeclaredMethods()).hasSize(1);
        assertThat(BankHolidaysApi.class.getDeclaredClasses()).isEmpty();
    }
}