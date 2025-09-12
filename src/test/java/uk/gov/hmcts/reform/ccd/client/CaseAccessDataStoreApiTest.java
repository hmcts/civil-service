package uk.gov.hmcts.reform.ccd.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

class CaseAccessDataStoreApiTest {

    @Test
    void interface_HasFeignClientAnnotation() {
        // Assert
        assertThat(CaseAccessDataStoreApi.class.isAnnotationPresent(FeignClient.class)).isTrue();
    }

    @Test
    void feignClient_HasCorrectConfiguration() {
        // Act
        FeignClient annotation = CaseAccessDataStoreApi.class.getAnnotation(FeignClient.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("ccd-access-data-store-api");
        assertThat(annotation.url()).isEqualTo("${core_case_data.api.url}");
        assertThat(annotation.configuration()).containsExactly(CoreCaseDataConfiguration.class);
    }

    @Test
    void interface_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(CaseAccessDataStoreApi.class.getModifiers())).isTrue();
    }

    @Test
    void interface_IsInterface() {
        // Assert
        assertThat(CaseAccessDataStoreApi.class.isInterface()).isTrue();
    }

    @Test
    void interface_HasThreeMethods() {
        // Act
        Method[] methods = CaseAccessDataStoreApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(3);
        assertThat(Arrays.stream(methods).map(Method::getName)).containsExactlyInAnyOrder(
            "addCaseUserRoles",
            "getUserRoles",
            "removeCaseUserRoles"
        );
    }

    @Test
    void interface_ExtendsNoInterfaces() {
        // Assert
        assertThat(CaseAccessDataStoreApi.class.getInterfaces()).isEmpty();
    }

    @Test
    void allMethods_ArePublic() {
        // Act
        Method[] methods = CaseAccessDataStoreApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void allMethods_AreAbstract() {
        // Act
        Method[] methods = CaseAccessDataStoreApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isAbstract(method.getModifiers()));
    }

    @Test
    void interface_UsesCorrectImports() {
        // This test verifies the interface uses the expected annotations and types
        Method[] methods = CaseAccessDataStoreApi.class.getDeclaredMethods();

        // Verify at least one method uses each expected annotation
        assertThat(methods).anyMatch(m -> m.isAnnotationPresent(PostMapping.class)).anyMatch(m -> m.isAnnotationPresent(
            GetMapping.class)).anyMatch(m -> m.isAnnotationPresent(GetMapping.class)).allMatch(m -> m.isAnnotationPresent(
            ResponseBody.class));
    }

    @Nested
    class AddCaseUserRolesTests {

        private Method addCaseUserRolesMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            addCaseUserRolesMethod = CaseAccessDataStoreApi.class.getMethod(
                "addCaseUserRoles",
                String.class,
                String.class,
                AddCaseAssignedUserRolesRequest.class
            );
        }

        @Test
        void addCaseUserRoles_HasPostMappingAnnotation() {
            // Assert
            assertThat(addCaseUserRolesMethod.isAnnotationPresent(PostMapping.class)).isTrue();
        }

        @Test
        void addCaseUserRoles_PostMappingConfiguration() {
            // Act
            PostMapping annotation = addCaseUserRolesMethod.getAnnotation(PostMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/case-users");
            assertThat(annotation.consumes()).containsExactly(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        void addCaseUserRoles_HasResponseBodyAnnotation() {
            // Assert
            assertThat(addCaseUserRolesMethod.isAnnotationPresent(ResponseBody.class)).isTrue();
        }

        @Test
        void addCaseUserRoles_ReturnsCorrectType() {
            // Assert
            assertThat(addCaseUserRolesMethod.getReturnType()).isEqualTo(AddCaseAssignedUserRolesResponse.class);
        }

        @Test
        void addCaseUserRoles_HasCorrectParameters() {
            // Act
            Parameter[] parameters = addCaseUserRolesMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(AddCaseAssignedUserRolesRequest.class);
        }

        @Test
        void addCaseUserRoles_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = addCaseUserRolesMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void addCaseUserRoles_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = addCaseUserRolesMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void addCaseUserRoles_ThirdParameterHasRequestBodyAnnotation() {
            // Act
            Parameter parameter = addCaseUserRolesMethod.getParameters()[2];

            // Assert
            assertThat(parameter.isAnnotationPresent(RequestBody.class)).isTrue();
        }
    }

    @Nested
    class GetUserRolesTests {

        private Method getUserRolesMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            getUserRolesMethod = CaseAccessDataStoreApi.class.getMethod(
                "getUserRoles",
                String.class,
                String.class,
                List.class
            );
        }

        @Test
        void getUserRoles_HasGetMappingAnnotation() {
            // Assert
            assertThat(getUserRolesMethod.isAnnotationPresent(GetMapping.class)).isTrue();
        }

        @Test
        void getUserRoles_GetMappingConfiguration() {
            // Act
            GetMapping annotation = getUserRolesMethod.getAnnotation(GetMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/case-users");
            assertThat(annotation.produces()).containsExactly(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        void getUserRoles_HasResponseBodyAnnotation() {
            // Assert
            assertThat(getUserRolesMethod.isAnnotationPresent(ResponseBody.class)).isTrue();
        }

        @Test
        void getUserRoles_ReturnsCorrectType() {
            // Assert
            assertThat(getUserRolesMethod.getReturnType()).isEqualTo(CaseAssignedUserRolesResource.class);
        }

        @Test
        void getUserRoles_HasCorrectParameters() {
            // Act
            Parameter[] parameters = getUserRolesMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(List.class);
        }

        @Test
        void getUserRoles_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = getUserRolesMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void getUserRoles_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = getUserRolesMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void getUserRoles_ThirdParameterHasRequestParamAnnotation() {
            // Act
            Parameter parameter = getUserRolesMethod.getParameters()[2];
            RequestParam annotation = parameter.getAnnotation(RequestParam.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("case_ids");
        }
    }

    @Nested
    class RemoveCaseUserRolesTests {

        private Method removeCaseUserRolesMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            removeCaseUserRolesMethod = CaseAccessDataStoreApi.class.getMethod(
                "removeCaseUserRoles",
                String.class,
                String.class,
                CaseAssignedUserRolesRequest.class
            );
        }

        @Test
        void removeCaseUserRoles_HasDeleteMappingAnnotation() {
            // Assert
            assertThat(removeCaseUserRolesMethod.isAnnotationPresent(DeleteMapping.class)).isTrue();
        }

        @Test
        void removeCaseUserRoles_DeleteMappingConfiguration() {
            // Act
            DeleteMapping annotation = removeCaseUserRolesMethod.getAnnotation(DeleteMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/case-users");
            assertThat(annotation.consumes()).containsExactly(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        void removeCaseUserRoles_HasResponseBodyAnnotation() {
            // Assert
            assertThat(removeCaseUserRolesMethod.isAnnotationPresent(ResponseBody.class)).isTrue();
        }

        @Test
        void removeCaseUserRoles_ReturnsCorrectType() {
            // Assert
            assertThat(removeCaseUserRolesMethod.getReturnType()).isEqualTo(AddCaseAssignedUserRolesResponse.class);
        }

        @Test
        void removeCaseUserRoles_HasCorrectParameters() {
            // Act
            Parameter[] parameters = removeCaseUserRolesMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(CaseAssignedUserRolesRequest.class);
        }

        @Test
        void removeCaseUserRoles_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = removeCaseUserRolesMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void removeCaseUserRoles_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = removeCaseUserRolesMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void removeCaseUserRoles_ThirdParameterHasRequestBodyAnnotation() {
            // Act
            Parameter parameter = removeCaseUserRolesMethod.getParameters()[2];

            // Assert
            assertThat(parameter.isAnnotationPresent(RequestBody.class)).isTrue();
        }
    }
}
