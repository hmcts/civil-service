package uk.gov.hmcts.reform.civil.client.sendletter.api.exception;

import org.springframework.http.HttpStatus;

/**
 * ServerHttpErrorException.
 */
public class ServerHttpErrorException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String message;

    /**
     * Constructor.
     * @param statusCode The status code
     * @param message The message
     */
    public ServerHttpErrorException(HttpStatus statusCode, String message) {
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
