package uk.gov.hmcts.reform.civil.ras.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.ras.model.QueryRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.ras.model.UpdateRoleAssignmentResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class RoleAssignmentsApiTest {

    @Test
    void interface_HasFeignClientAnnotation() {
        // Assert
        assertThat(RoleAssignmentsApi.class.isAnnotationPresent(FeignClient.class)).isTrue();
    }

    @Test
    void feignClient_HasCorrectConfiguration() {
        // Act
        FeignClient annotation = RoleAssignmentsApi.class.getAnnotation(FeignClient.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("am-role-assignment-service-api");
        assertThat(annotation.url()).isEqualTo("${role-assignment-service.api.url}");
        assertThat(annotation.configuration()).isEmpty();
    }

    @Test
    void interface_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(RoleAssignmentsApi.class.getModifiers())).isTrue();
    }

    @Test
    void interface_IsInterface() {
        // Assert
        assertThat(RoleAssignmentsApi.class.isInterface()).isTrue();
    }

    @Test
    void interface_HasCorrectConstants() throws NoSuchFieldException, IllegalAccessException {
        // Act
        Field serviceAuthField = RoleAssignmentsApi.class.getField("SERVICE_AUTHORIZATION");
        Field actorIdField = RoleAssignmentsApi.class.getField("ACTOR_ID");

        // Assert
        assertThat(serviceAuthField.get(null)).isEqualTo("ServiceAuthorization");
        assertThat(actorIdField.get(null)).isEqualTo("actorId");
    }

    @Nested
    class GetRoleAssignmentsByActorIdTests {

        private Method getRoleAssignmentsByActorIdMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            getRoleAssignmentsByActorIdMethod = RoleAssignmentsApi.class.getMethod(
                "getRoleAssignments",
                String.class,
                String.class,
                String.class
            );
        }

        @Test
        void getRoleAssignmentsByActorId_HasGetMappingAnnotation() {
            // Assert
            assertThat(getRoleAssignmentsByActorIdMethod.isAnnotationPresent(GetMapping.class)).isTrue();
        }

        @Test
        void getRoleAssignmentsByActorId_GetMappingConfiguration() {
            // Act
            GetMapping annotation = getRoleAssignmentsByActorIdMethod.getAnnotation(GetMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/am/role-assignments/actors/{actorId}");
            assertThat(annotation.consumes()).containsExactly(APPLICATION_JSON_VALUE);
            assertThat(annotation.headers()).containsExactly(CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE);
        }

        @Test
        void getRoleAssignmentsByActorId_ReturnsCorrectType() {
            // Assert
            assertThat(getRoleAssignmentsByActorIdMethod.getReturnType()).isEqualTo(RoleAssignmentServiceResponse.class);
        }

        @Test
        void getRoleAssignmentsByActorId_HasCorrectParameters() {
            // Act
            Parameter[] parameters = getRoleAssignmentsByActorIdMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(String.class);
        }

        @Test
        void getRoleAssignmentsByActorId_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = getRoleAssignmentsByActorIdMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void getRoleAssignmentsByActorId_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = getRoleAssignmentsByActorIdMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("ServiceAuthorization");
        }

        @Test
        void getRoleAssignmentsByActorId_ThirdParameterHasPathVariable() {
            // Act
            Parameter parameter = getRoleAssignmentsByActorIdMethod.getParameters()[2];
            PathVariable annotation = parameter.getAnnotation(PathVariable.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("actorId");
        }
    }

    @Nested
    class GetRoleAssignmentsByQueryTests {

        private Method getRoleAssignmentsByQueryMethod;

        @BeforeEach
        void setUp() {
            Method[] methods = RoleAssignmentsApi.class.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("getRoleAssignments") && method.getParameterCount() == 9) {
                    getRoleAssignmentsByQueryMethod = method;
                    break;
                }
            }
            assertThat(getRoleAssignmentsByQueryMethod).isNotNull();
        }

        @Test
        void getRoleAssignmentsByQuery_HasPostMappingAnnotation() {
            // Assert
            assertThat(getRoleAssignmentsByQueryMethod.isAnnotationPresent(PostMapping.class)).isTrue();
        }

        @Test
        void getRoleAssignmentsByQuery_PostMappingConfiguration() {
            // Act
            PostMapping annotation = getRoleAssignmentsByQueryMethod.getAnnotation(PostMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/am/role-assignments/query");
            assertThat(annotation.consumes()).containsExactly(APPLICATION_JSON_VALUE);
            assertThat(annotation.headers()).containsExactly(CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE);
        }

        @Test
        void getRoleAssignmentsByQuery_ReturnsCorrectType() {
            // Assert
            assertThat(getRoleAssignmentsByQueryMethod.getReturnType()).isEqualTo(RoleAssignmentServiceResponse.class);
        }

        @Test
        void getRoleAssignmentsByQuery_HasCorrectParameterCount() {
            // Assert
            assertThat(getRoleAssignmentsByQueryMethod.getParameterCount()).isEqualTo(9);
        }

        @Test
        void getRoleAssignmentsByQuery_FirstTwoParametersAreAuthHeaders() {
            // Act
            Parameter[] parameters = getRoleAssignmentsByQueryMethod.getParameters();

            // Assert
            RequestHeader authHeader = parameters[0].getAnnotation(RequestHeader.class);
            assertThat(authHeader.value()).isEqualTo(AUTHORIZATION);

            RequestHeader serviceAuthHeader = parameters[1].getAnnotation(RequestHeader.class);
            assertThat(serviceAuthHeader.value()).isEqualTo("ServiceAuthorization");
        }

        @Test
        void getRoleAssignmentsByQuery_HasOptionalHeaders() {
            // Act
            Parameter[] parameters = getRoleAssignmentsByQueryMethod.getParameters();

            // Assert correlation-id header
            RequestHeader correlationHeader = parameters[2].getAnnotation(RequestHeader.class);
            assertThat(correlationHeader.value()).isEqualTo("x-correlation-id");
            assertThat(correlationHeader.required()).isFalse();

            // Assert pageNumber header
            RequestHeader pageNumberHeader = parameters[3].getAnnotation(RequestHeader.class);
            assertThat(pageNumberHeader.value()).isEqualTo("pageNumber");
            assertThat(pageNumberHeader.required()).isFalse();
        }

        @Test
        void getRoleAssignmentsByQuery_HasRequestBodyParameter() {
            // Act
            Parameter[] parameters = getRoleAssignmentsByQueryMethod.getParameters();
            Parameter queryRequestParam = parameters[7];

            // Assert
            assertThat(queryRequestParam.getType()).isEqualTo(QueryRequest.class);
            assertThat(queryRequestParam.isAnnotationPresent(RequestBody.class)).isTrue();
            assertThat(queryRequestParam.getAnnotation(RequestBody.class).required()).isTrue();
        }

        @Test
        void getRoleAssignmentsByQuery_HasIncludeLabelsRequestParam() {
            // Act
            Parameter[] parameters = getRoleAssignmentsByQueryMethod.getParameters();
            Parameter includeLabelsParam = parameters[8];

            // Assert
            assertThat(includeLabelsParam.getType()).isEqualTo(Boolean.class);
            RequestParam annotation = includeLabelsParam.getAnnotation(RequestParam.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("includeLabels");
            assertThat(annotation.defaultValue()).isEqualTo("false");
        }
    }

    @Nested
    class CreateRoleAssignmentTests {

        private Method createRoleAssignmentMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            createRoleAssignmentMethod = RoleAssignmentsApi.class.getMethod(
                "createRoleAssignment",
                String.class,
                String.class,
                RoleAssignmentRequest.class
            );
        }

        @Test
        void createRoleAssignment_HasPostMappingAnnotation() {
            // Assert
            assertThat(createRoleAssignmentMethod.isAnnotationPresent(PostMapping.class)).isTrue();
        }

        @Test
        void createRoleAssignment_PostMappingConfiguration() {
            // Act
            PostMapping annotation = createRoleAssignmentMethod.getAnnotation(PostMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/am/role-assignments");
            assertThat(annotation.consumes()).containsExactly(APPLICATION_JSON_VALUE);
            assertThat(annotation.headers()).containsExactly(CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE);
        }

        @Test
        void createRoleAssignment_ReturnsCorrectType() {
            // Assert
            assertThat(createRoleAssignmentMethod.getReturnType()).isEqualTo(UpdateRoleAssignmentResponse.class);
        }

        @Test
        void createRoleAssignment_HasCorrectParameters() {
            // Act
            Parameter[] parameters = createRoleAssignmentMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(RoleAssignmentRequest.class);
        }

        @Test
        void createRoleAssignment_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = createRoleAssignmentMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void createRoleAssignment_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = createRoleAssignmentMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("ServiceAuthorization");
        }

        @Test
        void createRoleAssignment_ThirdParameterHasRequestBodyAnnotation() {
            // Act
            Parameter parameter = createRoleAssignmentMethod.getParameters()[2];

            // Assert
            assertThat(parameter.isAnnotationPresent(RequestBody.class)).isTrue();
        }
    }

    @Test
    void interface_HasThreeMethods() {
        // Act
        Method[] methods = RoleAssignmentsApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(3);
        assertThat(Arrays.stream(methods).map(Method::getName))
            .containsOnly("getRoleAssignments", "createRoleAssignment");
    }

    @Test
    void interface_ExtendsNoInterfaces() {
        // Assert
        assertThat(RoleAssignmentsApi.class.getInterfaces()).isEmpty();
    }

    @Test
    void allMethods_ArePublic() {
        // Act
        Method[] methods = RoleAssignmentsApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void allMethods_AreAbstract() {
        // Act
        Method[] methods = RoleAssignmentsApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isAbstract(method.getModifiers()));
    }

    @Test
    void interface_HasCorrectPackage() {
        // Assert
        assertThat(RoleAssignmentsApi.class.getPackage().getName()).isEqualTo("uk.gov.hmcts.reform.civil.ras.client");
    }
}
