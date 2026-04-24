package uk.gov.hmcts.reform.civil.scheduler.common;

public record ScheduledTaskFailure(Long caseId, String errorMessage) {
}
