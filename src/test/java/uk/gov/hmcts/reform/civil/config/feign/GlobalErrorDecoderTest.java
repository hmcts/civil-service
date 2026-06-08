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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalErrorDecoderTest {

    private GlobalErrorDecoder globalErrorDecoder;

    @Mock
    private ErrorDecoder delegate;

    @BeforeEach
    void setUp() {
        globalErrorDecoder = new GlobalErrorDecoder(delegate);
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

        Exception result = globalErrorDecoder.decode(methodKey, response);

        assertThat(result).isEqualTo(expectedException);
        verify(delegate).decode(methodKey, response);
    }

    @Test
    void shouldReturnOriginalExceptionIfAlreadyRetryable() {
        RetryableException retryableException = mock(RetryableException.class);
        when(delegate.decode(anyString(), any())).thenReturn(retryableException);

        Exception result = globalErrorDecoder.decode("method", mock(Response.class));

        assertThat(result).isEqualTo(retryableException);
    }

    @Test
    void shouldReturnOriginalExceptionIfResponseIsNull() {
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = globalErrorDecoder.decode("method", null);

        assertThat(result).isEqualTo(originalException);
    }

    @Test
    void shouldReturnOriginalExceptionIfRequestIsNull() {
        Response response = mock(Response.class);
        when(response.request()).thenReturn(null);
        Exception originalException = new Exception("error");
        when(delegate.decode(anyString(), any())).thenReturn(originalException);

        Exception result = globalErrorDecoder.decode("method", response);

        assertThat(result).isEqualTo(originalException);
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

        Exception result = globalErrorDecoder.decode("method", response);

        assertThat(result).isInstanceOf(RetryableException.class);
        RetryableException retryableException = (RetryableException) result;
        assertThat(retryableException.status()).isEqualTo(503);
        assertThat(retryableException.getMessage()).isEqualTo("original error");
        assertThat(retryableException.getCause()).isEqualTo(originalException);
        assertThat(retryableException.method()).isEqualTo(Request.HttpMethod.GET);
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

        Exception result = globalErrorDecoder.decode(methodKey, response);

        assertThat(result).isInstanceOf(RetryableException.class);
        assertThat(((RetryableException) result).status()).isEqualTo(429);
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

        Exception result = globalErrorDecoder.decode(methodKey, response);

        assertThat(result).isEqualTo(originalException);
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

        Exception result = globalErrorDecoder.decode("method", response);

        assertThat(result).isEqualTo(originalException);
    }

    @Test
    void shouldUseDefaultMessageIfExceptionOrMessageIsNull() {
        Request request = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        Response response = Response.builder()
            .status(503)
            .request(request)
            .build();
        when(delegate.decode(anyString(), any())).thenReturn(null);

        Exception result = globalErrorDecoder.decode("method", response);

        assertThat(result).isInstanceOf(RetryableException.class);
        assertThat(result.getMessage()).isEqualTo("Retryable Feign exception");
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

        Exception result = globalErrorDecoder.decode("methodKey", response);

        // if method is null, idempotent will be false (HC Method.isIdempotent(null) is false)
        // unless methodKey is retryableNonIdempotent
        assertThat(result).isEqualTo(originalException);
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

        Exception result = globalErrorDecoder.decode(methodKey, response);

        assertThat(result).isInstanceOf(RetryableException.class);
    }

    @Test
    void shouldWorkWithDefaultConstructor() {
        GlobalErrorDecoder decoder = new GlobalErrorDecoder();
        Response response = Response.builder()
            .status(404)
            .request(Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null))
            .build();
        
        Exception result = decoder.decode("method", response);
        assertThat(result).isNotNull();
    }
}
