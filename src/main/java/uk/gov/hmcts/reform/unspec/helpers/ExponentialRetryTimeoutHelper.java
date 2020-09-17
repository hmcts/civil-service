package uk.gov.hmcts.reform.unspec.helpers;

import org.springframework.validation.annotation.Validated;

@Validated
public class ExponentialRetryTimeoutHelper {

    private ExponentialRetryTimeoutHelper() {
    }

    /**
     * Calculates the current retry timeout value based on the remaining number of retries.
     *
     * @param startValue       the value of the retry timeout when total retries is equal to remaining retries.
     * @param totalRetries     the total number of times to retry. For a default Camunda external task this is 3.
     * @param remainingRetries the number of remaining retries. This will be the retries associated with the
     *                         external task {@link  org.camunda.bpm.client.task.ExternalTask}
     * @return a long value for retry timeout.
     */
    public static long calculateExponentialRetryTimeout(Integer startValue,
                                                        Integer totalRetries,
                                                        Integer remainingRetries) {
        if (remainingRetries > 0 && remainingRetries <= totalRetries) {
            return (long) (startValue * Math.pow(2, (double) totalRetries - (double) remainingRetries));
        }
        return 0L;
    }
}
