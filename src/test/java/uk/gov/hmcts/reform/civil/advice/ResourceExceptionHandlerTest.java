package uk.gov.hmcts.reform.civil.advice;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.service.notify.NotificationClientException;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceExceptionHandlerTest {

    @Mock
    private ContentCachingRequestWrapper contentCachingRequestWrapper;

    @InjectMocks
    private ResourceExceptionHandler handler;

    @BeforeEach
    void setUp() {
        String jsonString = "{ \"case_details\" : [{\"case_data\" : {\"array\" : [{\"key\" : \"value\"}]}, \"id\" : \"1234\"}]}";
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
