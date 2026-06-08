package uk.gov.hmcts.reform.civil.config.feign;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("java:S2975")
public class DefaultRetryer extends Retryer.Default {

    int count;

    public DefaultRetryer(int period, int maxPeriod, int maxAttempts) {
        super(period, maxPeriod, maxAttempts);
        this.count = 1;
    }

    public DefaultRetryer() {
        this(500, 5000, 3);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("## Feign retry attempt {} due to {} ", count++, e.getCause().getMessage());
        super.continueOrPropagate(e);
    }

    @Override
    @SuppressWarnings("java:S1182")
    public Retryer clone() {
        return new DefaultRetryer();
    }
}
