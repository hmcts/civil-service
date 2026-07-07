package uk.gov.hmcts.reform.civil.scheduler.common;

class ScheduledTaskInterruptedException extends RuntimeException {

    ScheduledTaskInterruptedException(String message, InterruptedException cause) {
        super(message, cause);
    }
}
