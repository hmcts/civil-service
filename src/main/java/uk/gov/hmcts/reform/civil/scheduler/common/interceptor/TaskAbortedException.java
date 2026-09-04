package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.Getter;

@Getter
public class TaskAbortedException extends RuntimeException {

    private final String reason;

    public TaskAbortedException(String reason) {
        super(reason);
        this.reason = reason;
    }
}
