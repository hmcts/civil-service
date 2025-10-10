package uk.gov.hmcts.reform.civil.cas.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.civil.cas.model.DecisionRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

class CaseAssignmentApiTest {

    @Test
    void interface_HasFeignClientAnnotation() {
        // Assert
        assertThat(CaseAssignmentApi.class.isAnnotationPresent(FeignClient.class)).isTrue();
    }

    @Test
    void feignClient_HasCorrectConfiguration() {
        // Act
        FeignClient annotation = CaseAssignmentApi.class.getAnnotation(FeignClient.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("case-assignment-api");
        assertThat(annotation.url()).isEqualTo("${aca.api.baseurl}");
        assertThat(annotation.configuration()).containsExactly(FeignClientProperties.FeignClientConfiguration.class);
    }

    @Test
    void interface_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(CaseAssignmentApi.class.getModifiers())).isTrue();
    }

    @Test
    void interface_IsInterface() {
        // Assert
        assertThat(CaseAssignmentApi.class.isInterface()).isTrue();
    }

    @Nested
    class ApplyDecisionTests {

        private Method applyDecisionMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            applyDecisionMethod = CaseAssignmentApi.class.getMethod(
                "applyDecision",
                String.class,
                String.class,
                DecisionRequest.class
            );
        }

        @Test
        void applyDecision_HasPostMappingAnnotation() {
            // Assert
            assertThat(applyDecisionMethod.isAnnotationPresent(PostMapping.class)).isTrue();
        }

        @Test
        void applyDecision_PostMappingConfiguration() {
            // Act
            PostMapping annotation = applyDecisionMethod.getAnnotation(PostMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/noc/apply-decision");
            assertThat(annotation.consumes()).containsExactly(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        void applyDecision_HasResponseBodyAnnotation() {
            // Assert
            assertThat(applyDecisionMethod.isAnnotationPresent(ResponseBody.class)).isTrue();
        }

        @Test
        void applyDecision_ReturnsCorrectType() {
            // Assert
            assertThat(applyDecisionMethod.getReturnType()).isEqualTo(AboutToStartOrSubmitCallbackResponse.class);
        }

        @Test
        void applyDecision_HasCorrectParameters() {
            // Act
            Parameter[] parameters = applyDecisionMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(DecisionRequest.class);
        }

        @Test
        void applyDecision_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = applyDecisionMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void applyDecision_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = applyDecisionMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void applyDecision_ThirdParameterHasRequestBodyAnnotation() {
            // Act
            Parameter parameter = applyDecisionMethod.getParameters()[2];

            // Assert
            assertThat(parameter.isAnnotationPresent(RequestBody.class)).isTrue();
        }
    }

    @Nested
    class CheckNocApprovalTests {

        private Method checkNocApprovalMethod;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            checkNocApprovalMethod = CaseAssignmentApi.class.getMethod(
                "checkNocApproval",
                String.class,
                String.class,
                CallbackRequest.class
            );
        }

        @Test
        void checkNocApproval_HasPostMappingAnnotation() {
            // Assert
            assertThat(checkNocApprovalMethod.isAnnotationPresent(PostMapping.class)).isTrue();
        }

        @Test
        void checkNocApproval_PostMappingConfiguration() {
            // Act
            PostMapping annotation = checkNocApprovalMethod.getAnnotation(PostMapping.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).containsExactly("/noc/check-noc-approval");
            assertThat(annotation.consumes()).containsExactly(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        void checkNocApproval_HasResponseBodyAnnotation() {
            // Assert
            assertThat(checkNocApprovalMethod.isAnnotationPresent(ResponseBody.class)).isTrue();
        }

        @Test
        void checkNocApproval_ReturnsCorrectType() {
            // Assert
            assertThat(checkNocApprovalMethod.getReturnType()).isEqualTo(SubmittedCallbackResponse.class);
        }

        @Test
        void checkNocApproval_HasCorrectParameters() {
            // Act
            Parameter[] parameters = checkNocApprovalMethod.getParameters();

            // Assert
            assertThat(parameters).hasSize(3);
            assertThat(parameters[0].getType()).isEqualTo(String.class);
            assertThat(parameters[1].getType()).isEqualTo(String.class);
            assertThat(parameters[2].getType()).isEqualTo(CallbackRequest.class);
        }

        @Test
        void checkNocApproval_FirstParameterHasAuthorizationHeader() {
            // Act
            Parameter parameter = checkNocApprovalMethod.getParameters()[0];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(AUTHORIZATION);
        }

        @Test
        void checkNocApproval_SecondParameterHasServiceAuthorizationHeader() {
            // Act
            Parameter parameter = checkNocApprovalMethod.getParameters()[1];
            RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);

            // Assert
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo(SERVICE_AUTHORIZATION);
        }

        @Test
        void checkNocApproval_ThirdParameterHasRequestBodyAnnotation() {
            // Act
            Parameter parameter = checkNocApprovalMethod.getParameters()[2];

            // Assert
            assertThat(parameter.isAnnotationPresent(RequestBody.class)).isTrue();
        }
    }

    @Test
    void interface_HasTwoMethods() {
        // Act
        Method[] methods = CaseAssignmentApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).hasSize(2);
        assertThat(Arrays.stream(methods).map(Method::getName))
            .containsExactlyInAnyOrder("applyDecision", "checkNocApproval");
    }

    @Test
    void interface_ExtendsNoInterfaces() {
        // Assert
        assertThat(CaseAssignmentApi.class.getInterfaces()).isEmpty();
    }

    @Test
    void allMethods_ArePublic() {
        // Act
        Method[] methods = CaseAssignmentApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()));
    }

    @Test
    void allMethods_AreAbstract() {
        // Act
        Method[] methods = CaseAssignmentApi.class.getDeclaredMethods();

        // Assert
        assertThat(methods).allMatch(method -> java.lang.reflect.Modifier.isAbstract(method.getModifiers()));
    }

    @Test
    void interface_UsesCorrectImports() {
        // This test verifies the interface uses the expected annotations and types
        Method[] methods = CaseAssignmentApi.class.getDeclaredMethods();

        // Verify all methods use PostMapping
        assertThat(methods).allMatch(m -> m.isAnnotationPresent(PostMapping.class)).allMatch(m -> m.isAnnotationPresent(ResponseBody.class));
    }

    @Test
    void interface_HasCorrectPackage() {
        // Assert
        assertThat(CaseAssignmentApi.class.getPackage().getName()).isEqualTo("uk.gov.hmcts.reform.civil.cas.client");
    }
}
