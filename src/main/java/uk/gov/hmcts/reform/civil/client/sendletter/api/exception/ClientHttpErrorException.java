package uk.gov.hmcts.reform.civil.client.sendletter.api.exception;

import org.springframework.http.HttpStatus;

/**
 * ClientHttpErrorException.
 */
public class ClientHttpErrorException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String message;

    /**
     * Constructor.
     * @param statusCode The status code
     * @param message The message
     */
    public ClientHttpErrorException(HttpStatus statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    /**
     * Get the status code.
     * @return The status code
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * Get the message.
     * @return The message
     */
    public String getMessage() {
        return message;
    }
}
