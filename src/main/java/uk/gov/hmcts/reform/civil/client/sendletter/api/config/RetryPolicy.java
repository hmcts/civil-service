package uk.gov.hmcts.reform.civil.client.sendletter.api.config;


import org.springframework.http.HttpStatus;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import uk.gov.hmcts.reform.civil.client.sendletter.api.exception.ClientHttpErrorException;

import java.util.List;
import java.util.Optional;

/**
 * Retry policy.
 */
public class RetryPolicy extends ExceptionClassifierRetryPolicy {

    /**
     * Constructor.
     * @param maxAttempts The max attempts
     * @param retryStatuses The retry statuses
     */
    public RetryPolicy(int maxAttempts, List<HttpStatus> retryStatuses) {
        final NeverRetryPolicy neverRetryPolicy = new NeverRetryPolicy();
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(maxAttempts);

        this.setExceptionClassifier(classifiable -> {

            if (classifiable instanceof ClientHttpErrorException) {
                Optional<HttpStatus> retryStatus = retryStatuses.stream()
                        .filter(value -> value == ((ClientHttpErrorException) classifiable).getStatusCode()).findAny();
                if (retryStatus.isPresent()) {
                    return simpleRetryPolicy;
                }
                return neverRetryPolicy;
            }
            return neverRetryPolicy;
        });
    }
}
