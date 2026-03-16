package uk.gov.hmcts.reform.civil.client.sendletter;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.client.sendletter.api.exception.ClientHttpErrorException;
import uk.gov.hmcts.reform.civil.client.sendletter.api.exception.ServerHttpErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Custom error decoder for Feign client.
 */
public class CustomFeignErrorDecoder implements ErrorDecoder {
    private ErrorDecoder delegate = new ErrorDecoder.Default();

    /**
     * Decodes the response and returns an exception.
     *
     * @param methodKey The method key
     * @param response The response
     * @return The exception
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpHeaders responseHeaders = new HttpHeaders();
        response.headers()
            .forEach((key, value) -> responseHeaders.put(key, new ArrayList<>(value)));

        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String statusText = Optional.ofNullable(response.reason()).orElse(statusCode.getReasonPhrase());

        byte[] responseBody;

        if (response.body() != null && response.body().length() != null) {
            try (InputStream body = response.body().asInputStream()) {
                responseBody = IOUtils.toByteArray(body);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process response body.", e);
            }
        } else {
            responseBody = new byte[0]; // Initialize an empty byte array if response body is null
        }

        if (statusCode.is4xxClientError()) {
            return new ClientHttpErrorException(
                statusCode,
                String.format("%s %s: %s", statusCode.value(), statusText,
                              new String(responseBody, StandardCharsets.UTF_8)
                )
            );
        }

        if (statusCode.is5xxServerError()) {
            return new ServerHttpErrorException(statusCode, String.format("%s %s", statusCode.value(), statusText));
        }

        return delegate.decode(methodKey, response);
    }
}
