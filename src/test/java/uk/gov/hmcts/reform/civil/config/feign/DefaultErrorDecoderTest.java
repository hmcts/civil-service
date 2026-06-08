package uk.gov.hmcts.reform.civil.config.feign;

import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultErrorDecoderTest {

    private DefaultErrorDecoder defaultErrorDecoder;

    @Mock
    private ErrorDecoder delegate;

    @Mock
    private FeignErrorTelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        defaultErrorDecoder = new DefaultErrorDecoder(delegate, telemetryService);
    }

    @Test
    void shouldCallDelegateDecode() {
        Response response = Response.builder()
            .status(400)
            .reason("Bad Request")
            .request(mock(Request.class))
            .build();
        String methodKey = "someMethod";
        Exception expectedException = new Exception("Delegate Exception");
        when(delegate.decode(methodKey, response)).thenReturn(expectedException);

        Exception result = defaultErrorDecoder.decode(methodKey, response);

        assertThat(result).isEqualTo(expectedException);
        verify(delegate).decode(methodKey, response);
        verify(telemetryService).trackErrorClassification(eq(methodKey), same(response), same(expectedException));
    }

    @Test
    void shouldReturnOriginalExceptionIfAlreadyRetryable() {
        RetryableException retryableException = mock(RetryableException.class);
        when(delegate.decode(anyString(), any())).thenReturn(retryableException);

        Exception result = defaultErrorDecoder.decode("method", mock(Response.class));

        assertThat(result).isEqualTo(retryableException);
        verify(telemetryService).trackErrorClassification(eq("method"), any(), same(retryableException));
    }

    @Test
    void shouldReturnOriginalExceptionIfResponseIsNull() {
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = defaultErrorDecoder.decode("method", null);

        assertThat(result).isEqualTo(originalException);
        verify(telemetryService).trackErrorClassification(eq("method"), isNull(), same(originalException));
    }

    @Test
    void shouldReturnOriginalExceptionIfRequestIsNull() {
        Response response = mock(Response.class);
        when(response.request()).thenReturn(null);
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = defaultErrorDecoder.decode("method", response);

        assertThat(result).isEqualTo(originalException);
        verify(telemetryService).trackErrorClassification(eq("method"), same(response), same(originalException));
    }

    @Test
    void shouldMapToRetryableExceptionForIdempotentMethodAndRetryableStatus() {
        Request request = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        Response response = Response.builder()
            .status(503)
            .request(request)
            .build();
        Exception originalException = new Exception("original error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = defaultErrorDecoder.decode("method", response);

        assertThat(result).isInstanceOf(RetryableException.class);
        RetryableException retryableException = (RetryableException) result;
        assertThat(retryableException.status()).isEqualTo(503);
        assertThat(retryableException.getMessage()).isEqualTo("original error");
        assertThat(retryableException.getCause()).isEqualTo(originalException);
        assertThat(retryableException.method()).isEqualTo(Request.HttpMethod.GET);
        verify(telemetryService).trackErrorClassification(eq("method"), same(response),
            argThat(throwable -> throwable instanceof RetryableException
                && throwable.getCause() == originalException));
    }

    @Test
    void shouldMapToRetryableExceptionForNonIdempotentButAllowedMethod() {
        // CoreCaseDataApi#searchCases(String,String,String,String) is allowed in FeignRetryUtils
        String methodKey = "CoreCaseDataApi#searchCases(String,String,String,String)";
        Request request = Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, null, null);
        Response response = Response.builder()
            .status(429)
            .request(request)
            .build();
        when(delegate.decode(anyString(), any())).thenReturn(new Exception("too many requests"));

        Exception result = defaultErrorDecoder.decode(methodKey, response);

        assertThat(result).isInstanceOf(RetryableException.class);
        assertThat(((RetryableException) result).status()).isEqualTo(429);
        verify(telemetryService).trackErrorClassification(eq(methodKey), same(response),
            argThat(RetryableException.class::isInstance));
    }

    @Test
    void shouldNotMapToRetryableExceptionForNonIdempotentMethodAndNotAllowedMethod() {
        String methodKey = "Other#method";
        Request request = Request.create(Request.HttpMethod.POST, "url", Collections.emptyMap(), null, null, null);
        Response response = Response.builder()
            .status(503)
            .request(request)
            .build();
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = defaultErrorDecoder.decode(methodKey, response);

        assertThat(result).isEqualTo(originalException);
        verify(telemetryService).trackErrorClassification(eq(methodKey), same(response), same(originalException));
    }

    @Test
    void shouldNotMapToRetryableExceptionForNonRetryableStatus() {
        Request request = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        Response response = Response.builder()
            .status(400)
            .request(request)
            .build();
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = defaultErrorDecoder.decode("method", response);

        assertThat(result).isEqualTo(originalException);
        verify(telemetryService).trackErrorClassification(eq("method"), same(response), same(originalException));
    }

    @Test
    void shouldUseDefaultMessageIfExceptionOrMessageIsNull() {
        Request request = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        Response response = Response.builder()
            .status(503)
            .request(request)
            .build();
        when(delegate.decode(anyString(), any())).thenReturn(null);

        Exception result = defaultErrorDecoder.decode("method", response);

        assertThat(result).isInstanceOf(RetryableException.class);
        assertThat(result.getMessage()).isEqualTo("Retryable Feign exception");
        verify(telemetryService).trackErrorClassification(eq("method"), same(response),
            argThat(throwable -> throwable instanceof RetryableException
                && "Retryable Feign exception".equals(throwable.getMessage())));
    }

    @Test
    void shouldHandleNullHttpMethod() {
        Request request = mock(Request.class);
        when(request.httpMethod()).thenReturn(null);
        Response response = Response.builder()
            .status(503)
            .request(request)
            .build();
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = defaultErrorDecoder.decode("methodKey", response);

        // if method is null, idempotent will be false (HC Method.isIdempotent(null) is false)
        // unless methodKey is retryableNonIdempotent
        assertThat(result).isEqualTo(originalException);
        verify(telemetryService).trackErrorClassification(eq("methodKey"), same(response), same(originalException));
    }

    @Test
    void shouldHandleNullHttpMethodButAllowedMethodKey() {
        String methodKey = "CoreCaseDataApi#searchCases(String,String,String,String)";
        Request request = mock(Request.class);
        when(request.httpMethod()).thenReturn(null);
        Response response = Response.builder()
            .status(429)
            .request(request)
            .build();
        when(delegate.decode(anyString(), any())).thenReturn(new Exception("error"));

        Exception result = defaultErrorDecoder.decode(methodKey, response);

        assertThat(result).isInstanceOf(RetryableException.class);
        verify(telemetryService).trackErrorClassification(eq(methodKey), same(response),
            argThat(RetryableException.class::isInstance));
    }

    @Test
    void shouldWorkWithDefaultConstructor() {
        DefaultErrorDecoder decoder = new DefaultErrorDecoder(new ErrorDecoder.Default());
        Response response = Response.builder()
            .status(404)
            .request(Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null))
            .build();

        Exception result = decoder.decode("method", response);
        assertThat(result).isNotNull();
    }
}
