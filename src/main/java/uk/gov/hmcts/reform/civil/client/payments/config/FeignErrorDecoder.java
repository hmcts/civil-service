package uk.gov.hmcts.reform.civil.client.payments.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import uk.gov.hmcts.reform.civil.client.payments.InvalidPaymentRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    public static final String DUPLICATE_PAYMENT = "Duplicate Payment";

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        byte[] bytes = getBytes(response);
        Response output = response.toBuilder().body(bytes).build();
        final String responseBody = new String(bytes, StandardCharsets.UTF_8);

        if (response.status() == 400 && responseBody.equalsIgnoreCase(DUPLICATE_PAYMENT)) {
            log.error("Error took place when using Feign client to send HTTP Request."
                    + " Status code "
                    + response.status()
                    + ", methodKey = "
                    + methodKey);
            return new InvalidPaymentRequestException(responseBody);
        }
        return defaultErrorDecoder.decode(methodKey, output);
    }

    private byte[] getBytes(Response response) {
        try {
            InputStream inputStream = response.body().asInputStream();
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("Failed to read the response body with error: ", e);
        }
        return null;
    }
}
