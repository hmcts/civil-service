package uk.gov.hmcts.reform.civil.prd.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

class OrganisationApiTest {

    @Test
    void interface_HasFeignClientAnnotation() {
        // Assert
        assertThat(OrganisationApi.class.isAnnotationPresent(FeignClient.class)).isTrue();
    }

    @Test
    void feignClient_HasCorrectConfiguration() {
        // Act
        FeignClient annotation = OrganisationApi.class.getAnnotation(FeignClient.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("rd-professional-api");
        assertThat(annotation.url()).isEqualTo("${rd_professional.api.url}");
        assertThat(annotation.configuration()).isEmpty();
    }

    @Test
    void interface_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(OrganisationApi.class.getModifiers())).isTrue();
    }

    @Test
    void interface_IsInterface() {
        // Assert
        assertThat(OrganisationApi.class.isInterface()).isTrue();
    }

    @Nested
    class FindUserOrganisationTests {

        private Method findUserOrganisationMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            findUserOrganisationMethod = OrganisationApi.class.getMethod(
                "findUserOrganisation",
                String.class,
                String.class
            );
        }

        @Test
        void findUserOrganisation_HasGetMappingAnnotation() {
            // Assert
            assertThat(findUserOrganisationMethod.isAnnotationPresent(GetMapping.class)).isTrue();
        }

        @Test
        void findUserOrganisation_GetMappingConfiguration() {
            // Act
            GetMapping annotation = findUserOrganisationMethod.getAnnotation(GetMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/refdata/external/v1/organisations");
        }

        @Test
        void findUserOrganisation_ReturnsCorrectType() {
            // Assert
            assertThat(findUserOrganisationMethod.getReturnType()).isEqualTo(Organisation.class);
        }

        @Test
        void findUserOrganisation_HasCorrectParameters() {
            // Act
            Parameter[] parameters = findUserOrganisationMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(2);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
        }

        @Test
        void findUserOrganisation_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = findUserOrganisationMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void findUserOrganisation_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = findUserOrganisationMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void findUserOrganisation_HasNoResponseBodyAnnotation() {
            // Assert - GetMapping methods in Feign don't need @ResponseBody
            assertThat(findUserOrganisationMethod.isAnnotationPresent(ResponseBody.class)).isFalse();
        }
    }

    @Nested
    class FindOrganisationByIdTests {

        private Method findOrganisationByIdMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            findOrganisationByIdMethod = OrganisationApi.class.getMethod(
                "findOrganisationById",
                String.class,
                String.class,
                String.class
            );
        }

        @Test
        void findOrganisationById_HasGetMappingAnnotation() {
            // Assert
            assertThat(findOrganisationByIdMethod.isAnnotationPresent(GetMapping.class)).isTrue();
        }

        @Test
        void findOrganisationById_GetMappingConfiguration() {
            // Act
            GetMapping annotation = findOrganisationByIdMethod.getAnnotation(GetMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/refdata/internal/v1/organisations");
        }

        @Test
        void findOrganisationById_ReturnsCorrectType() {
            // Assert
            assertThat(findOrganisationByIdMethod.getReturnType()).isEqualTo(Organisation.class);
        }

        @Test
        void findOrganisationById_HasCorrectParameters() {
            // Act
            Parameter[] parameters = findOrganisationByIdMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(String.class);
        }

        @Test
        void findOrganisationById_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = findOrganisationByIdMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void findOrganisationById_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = findOrganisationByIdMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void findOrganisationById_ThirdParameterHasRequestParamAnnotation() {
            // Act
            Parameter parameter = findOrganisationByIdMethod.getParameters()[2];
            RequestParam annotation = parameter.getAnnotation(RequestParam.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("id");
        }

        @Test
        void findOrganisationById_HasNoResponseBodyAnnotation() {
            // Assert - GetMapping methods in Feign don't need @ResponseBody
            assertThat(findOrganisationByIdMethod.isAnnotationPresent(ResponseBody.class)).isFalse();
        }
    }

    @Test
    void interface_HasTwoMethods() {
        // Act
        Method[] methods = OrganisationApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(2);
        assertThat(Arrays.stream(methods).map(Method::getName))
            .containsExactlyInAnyOrder("findUserOrganisation", "findOrganisationById");
    }

    @Test
    void interface_ExtendsNoInterfaces() {
        // Assert
        assertThat(OrganisationApi.class.getInterfaces()).isEmpty();
    }

    @Test
    void allMethods_ArePublic() {
        // Act
        Method[] methods = OrganisationApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void allMethods_AreAbstract() {
        // Act
        Method[] methods = OrganisationApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isAbstract(method.getModifiers()));
    }

    @Test
    void interface_UsesCorrectImports() {
        // This test verifies the interface uses the expected annotations and types
        Method[] methods = OrganisationApi.class.getDeclaredMethods();

        // Verify all methods use GetMapping
        assertThat(methods).allMatch(m -> m.isAnnotationPresent(GetMapping.class));
    }

    @Test
    void interface_HasCorrectPackage() {
        // Assert
        assertThat(OrganisationApi.class.getPackage().getName()).isEqualTo("uk.gov.hmcts.reform.civil.prd.client");
    }
}
