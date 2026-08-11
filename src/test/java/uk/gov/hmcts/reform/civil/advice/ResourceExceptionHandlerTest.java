package uk.gov.hmcts.reform.civil.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.model.CallbackErrorResponse;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamIdamException;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.service.notify.NotificationClientException;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceExceptionHandlerTest {

    @Mock
    private ContentCachingRequestWrapper contentCachingRequestWrapper;

    @InjectMocks
    private ResourceExceptionHandler handler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new ResourceExceptionHandler(objectMapper);
        String jsonString = "{ \"case_details\" : {\"case_data\" : {\"array\" : [{\"key\" : \"value\"}]}, \"id\" : \"1234\"}}";
        when(contentCachingRequestWrapper.getHeader("user-id")).thenReturn("4321");
        when(contentCachingRequestWrapper.getContentAsByteArray()).thenReturn(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldReturnNotFound_whenCallbackExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            handler.notFound(
                new CallbackException("expected exception for missing callback handler"),
                contentCachingRequestWrapper
            ),
            HttpStatus.NOT_FOUND
        );
    }

    @Test
    void shouldReturnPreconditionFailed_whenStateFlowExceptionThrown() {
        testTemplate(
            "expected exception for state flow",
            handler.incorrectStateFlowOrIllegalArgument(
                new StateFlowException("expected exception for state flow"),
                contentCachingRequestWrapper
            ),
            HttpStatus.PRECONDITION_FAILED
        );
    }

    @Test
    void shouldReturnUnauthorized_whenFeignExceptionUnauthorizedExceptionThrown() {
        testTemplate(
            "expected exception for feing unauthorized",
            handler.unauthorizedFeign(new FeignException.Unauthorized(
                "expected exception for feing unauthorized",
                Mockito.mock(feign.Request.class),
                new byte[]{},
                Collections.emptyMap()
            ), contentCachingRequestWrapper),
            HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    void shouldReturnMethodNotAllowed_whenUnknownHostException() {
        testTemplate(
            "expected exception for unknown host",
            handler.unknownHostAndInvalidPayment(
                new UnknownHostException("expected exception for unknown host"),
                contentCachingRequestWrapper
            ),
            HttpStatus.NOT_ACCEPTABLE
        );
    }

    @Test
    void shouldReturnMethodNotAllowed_whenInvalidPaymentRequestExceptionException() {
        testTemplate(
            "expected exception for invalid payment request",
            handler.unknownHostAndInvalidPayment(new UnknownHostException(
                "expected exception for invalid payment request"), contentCachingRequestWrapper),
            HttpStatus.NOT_ACCEPTABLE
        );
    }

    @Test
    void shouldReturnForbidden_whenFeignExceptionForbiddenExceptionThrown() {
        testTemplate(
            "expected exception for feing forbidden",
            handler.forbiddenFeign(new FeignException.Unauthorized(
                "expected exception for feing forbidden",
                Mockito.mock(feign.Request.class),
                new byte[]{},
                Collections.emptyMap()
            ), contentCachingRequestWrapper),
            HttpStatus.FORBIDDEN
        );
    }

    @Test
    void shouldReturnMethodNotAllowed_whenNoSuchMethodErrorThrown() {
        testTemplate(
            "expected exception for no such method error",
            handler.noSuchMethodError(
                new NoSuchMethodError("expected exception for no such method error"),
                contentCachingRequestWrapper
            ),
            HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @Test
    void shouldReturnPreconditionFailed_whenIllegalArgumentExceptionThrown() {
        testTemplate(
            "expected exception for illegal argument exception",
            handler.incorrectStateFlowOrIllegalArgument(
                new IllegalArgumentException("expected exception for illegal argument exception"),
                contentCachingRequestWrapper
            ),
            HttpStatus.PRECONDITION_FAILED
        );
    }

    @Test
    void shouldReturnBadRequest_whenHttpClientErrorExceptionThrown() {
        testTemplate(
            "expected exception for client error bad request",
            handler.badRequest(
                new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "expected exception for client error bad request"
                ),
                contentCachingRequestWrapper
            ),
            HttpStatus.BAD_REQUEST
        );
    }

    @Test
    void testFeignExceptionGatewayTimeoutException() {
        testTemplate(
            "gateway time out message",
            handler.handleFeignExceptionGatewayTimeout(
                new FeignException.GatewayTimeout(
                    "gateway time out message",
                    Mockito.mock(feign.Request.class),
                    new byte[]{},
                    Collections.emptyMap()
                ),
                contentCachingRequestWrapper
            ),
            HttpStatus.GATEWAY_TIMEOUT
        );
    }

    @Test
    void testClientAbortException() {
        testTemplate(
            "ClosedChannelException",
            handler.handleClientAbortException(
                new FeignException.InternalServerError(
                    "ClosedChannelException",
                    Mockito.mock(feign.Request.class),
                    new byte[]{},
                    Collections.emptyMap()
                ),
                contentCachingRequestWrapper
            ),
            HttpStatus.REQUEST_TIMEOUT
        );
    }

    @Test
    void testHandleNotificationClientException() {
        testTemplate(
            "expected exception from notification api",
            handler.handleNotificationClientException(new NotificationClientException(
                "expected exception from notification api"), contentCachingRequestWrapper),
            HttpStatus.FAILED_DEPENDENCY
        );
    }

    @Test
    void shouldReturnFailedDependency_whenUpstreamIdamExceptionThrown() {
        testTemplate(
            "IDAM temporarily unavailable",
            handler.handleUpstreamIdamException(
                new UpstreamIdamException("IDAM temporarily unavailable"),
                contentCachingRequestWrapper
            ),
            HttpStatus.FAILED_DEPENDENCY
        );
    }

    @Test
    void testHandleFeignNotFoundException() {
        testTemplate(
            "expected exception for feign not found",
            handler.feignExceptionNotFound(
                new FeignException.NotFound(
                    "expected exception for feign not found",
                    Mockito.mock(feign.Request.class),
                    new byte[]{},
                    Collections.emptyMap()
                ),
                contentCachingRequestWrapper
            ),
            HttpStatus.NOT_FOUND
        );
    }

    @Test
    void shouldReturnExpectationFailed_whenJsonSchemaValidationExceptionThrown() {
        testTemplate(
            "expected exception from json schema rpa",
            handler.handleJsonSchemaValidationException(new JsonSchemaValidationException(
                "expected exception from json schema rpa",
                new Throwable()
            ), contentCachingRequestWrapper),
            HttpStatus.EXPECTATION_FAILED
        );
    }

    @Test
    void shouldReturnUnprocessableEntity_withParsedCallbackErrorResponse_whenFeign422BodyIsValidJson()
        throws Exception {
        CallbackErrorResponse expected = new CallbackErrorResponse();
        expected.setCallbackErrors(List.of("error one", "error two"));
        expected.setCallbackWarnings(List.of("warn one"));
        expected.setException("uk.gov.hmcts.ccd.endpoint.exceptions.ApiException");
        expected.setError("Unprocessable Entity");
        expected.setMessage("Unable to proceed because there are one or more callback Errors or Warnings");
        byte[] body = objectMapper.writeValueAsBytes(expected);

        testTemplate(
            "CallbackErrorResponse(exception=uk.gov.hmcts.ccd.endpoint.exceptions.ApiException, status=null, " +
                "error=Unprocessable Entity, message=Unable to proceed because there are one or more callback " +
                "Errors or Warnings, details=null, callbackErrors=[error one, error two], callbackWarnings=[warn one])",
            handler.unprocessableEntity(new FeignException.UnprocessableEntity(
                "unprocessable",
                Mockito.mock(feign.Request.class),
                body,
                Collections.emptyMap()
            ), contentCachingRequestWrapper),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void shouldReturnUnprocessableEntity_withFallbackMessage_whenFeign422BodyIsNotValidJson() {
        String invalidJson = """
        {
          "callbackErrors": [
            "Validation failed",
            "Missing required field: caseId"
          ]
            """; // missing closing brace

        testTemplate(
            "CallbackErrorResponse(exception=null, status=null, error=null, message=null, details=null, callbackErrors=[Unable to parse error response], callbackWarnings=null)",
            handler.unprocessableEntity(new FeignException.UnprocessableEntity(
                "unprocessable",
                Mockito.mock(feign.Request.class),
                invalidJson.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
            ), contentCachingRequestWrapper),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Test
    void shouldDeserializeDetailsObject_whenFeign422BodyContainsDetails() {
        String json = """
            {
              "exception": "uk.gov.hmcts.ccd.endpoint.exceptions.ApiException",
              "error": "Unprocessable Entity",
              "message": "Unable to proceed because there are one or more callback Errors or Warnings",
              "details": { "field": "caseId", "reason": "required" },
              "callbackErrors": ["error one"]
            }
            """;

        ResponseEntity<?> response = handler.unprocessableEntity(new FeignException.UnprocessableEntity(
            "unprocessable",
            Mockito.mock(feign.Request.class),
            json.getBytes(StandardCharsets.UTF_8),
            Collections.emptyMap()
        ), contentCachingRequestWrapper);

        assertThat(response.getStatusCode()).isSameAs(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isInstanceOf(CallbackErrorResponse.class);
        CallbackErrorResponse body = (CallbackErrorResponse) response.getBody();
        assertThat(body.getDetails()).isNotNull();
        assertThat(body.getDetails().toString()).contains("caseId").contains("required");
        assertThat(body.getCallbackErrors()).containsExactly("error one");
    }

    private <E extends Throwable> void testTemplate(
        String message,
        ResponseEntity<?> result,
        HttpStatus expectedStatus
    ) {

        assertThat(result.getStatusCode()).isSameAs(expectedStatus);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains(message);
    }
}
