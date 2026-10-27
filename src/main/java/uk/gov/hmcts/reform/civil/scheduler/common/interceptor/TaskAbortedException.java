package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.Getter;

@Getter
public class TaskAbortedException extends RuntimeException {

    private final AbortCategory category;
    private final String reason;

    public TaskAbortedException(AbortCategory category, String reason) {
        super(reason);
        this.category = category;
        this.reason = reason;
    }

    public TaskAbortedException(String reason) {
        this(AbortCategory.OTHER, reason);
    }
}
