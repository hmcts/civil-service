package uk.gov.hmcts.reform.civil.crd.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

class ListOfValuesApiTest {

    private Method findCategoryMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        findCategoryMethod = ListOfValuesApi.class.getMethod(
            "findCategoryByCategoryIdAndServiceId",
            String.class,
            String.class,
            String.class,
            String.class
        );
    }

    @Test
    void interface_HasFeignClientAnnotation() {
        // Assert
        assertThat(ListOfValuesApi.class.isAnnotationPresent(FeignClient.class)).isTrue();
    }

    @Test
    void feignClient_HasCorrectConfiguration() {
        // Act
        FeignClient annotation = ListOfValuesApi.class.getAnnotation(FeignClient.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("rd-commondata-api");
        assertThat(annotation.url()).isEqualTo("${rd_commondata.api.url}");
        assertThat(annotation.configuration()).isEmpty();
    }

    @Test
    void interface_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(ListOfValuesApi.class.getModifiers())).isTrue();
    }

    @Test
    void interface_IsInterface() {
        // Assert
        assertThat(ListOfValuesApi.class.isInterface()).isTrue();
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_HasGetMappingAnnotation() {
        // Assert
        assertThat(findCategoryMethod.isAnnotationPresent(GetMapping.class)).isTrue();
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_GetMappingConfiguration() {
        // Act
        GetMapping annotation = findCategoryMethod.getAnnotation(GetMapping.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly("/refdata/commondata/lov/categories/{category-id}");
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_ReturnsCorrectType() {
        // Assert
        assertThat(findCategoryMethod.getReturnType()).isEqualTo(CategorySearchResult.class);
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_HasCorrectParameterCount() {
        // Act
        Parameter[] parameters = findCategoryMethod.getParameters();

        // Assert
        assertThat(parameters).hasSize(4);
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_FirstParameterIsPathVariable() {
        // Act
        Parameter parameter = findCategoryMethod.getParameters()[0];
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);

        // Assert
        assertThat(parameter.getType()).isEqualTo(String.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("category-id");
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_SecondParameterIsRequestParam() {
        // Act
        Parameter parameter = findCategoryMethod.getParameters()[1];
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);

        // Assert
        assertThat(parameter.getType()).isEqualTo(String.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("serviceId");
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_ThirdParameterIsAuthorizationHeader() {
        // Act
        Parameter parameter = findCategoryMethod.getParameters()[2];
        RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

        // Assert
        assertThat(parameter.getType()).isEqualTo(String.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
    }

    @Test
    void findCategoryByCategoryIdAndServiceId_FourthParameterIsServiceAuthorizationHeader() {
        // Act
        Parameter parameter = findCategoryMethod.getParameters()[3];
        RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

        // Assert
        assertThat(parameter.getType()).isEqualTo(String.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
    }

    @Test
    void interface_HasOneMethod() {
        // Act
        Method[] methods = ListOfValuesApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(1);
        assertThat(methods[0].getName()).isEqualTo("findCategoryByCategoryIdAndServiceId");
    }

    @Test
    void interface_ExtendsNoInterfaces() {
        // Assert
        assertThat(ListOfValuesApi.class.getInterfaces()).isEmpty();
    }

    @Test
    void method_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(findCategoryMethod.getModifiers())).isTrue();
    }

    @Test
    void method_IsAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(findCategoryMethod.getModifiers())).isTrue();
    }

    @Test
    void interface_HasCorrectPackage() {
        // Assert
        assertThat(ListOfValuesApi.class.getPackage().getName()).isEqualTo("uk.gov.hmcts.reform.civil.crd.client");
    }

    @Test
    void method_HasNoResponseBodyAnnotation() {
        // Assert - GetMapping methods in Feign don't need @ResponseBody
        assertThat(findCategoryMethod.isAnnotationPresent(ResponseBody.class)).isFalse();
    }

    @Test
    void method_ParametersHaveCorrectOrder() {
        // Act
        Parameter[] parameters = findCategoryMethod.getParameters();

        // Assert - Verify the order matches the method signature
        assertThat(parameters[0].isAnnotationPresent(PathVariable.class)).isTrue();
        assertThat(parameters[1].isAnnotationPresent(RequestParam.class)).isTrue();
        assertThat(parameters[2].isAnnotationPresent(RequestHeader.class)).isTrue();
        assertThat(parameters[3].isAnnotationPresent(RequestHeader.class)).isTrue();
    }
}
