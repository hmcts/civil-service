package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.Value;

@Value
public class ScheduledTaskEventConfiguration {

    String schedulerName;
    String jobStartedEvent;
    String caseProcessedEvent;
    String caseFailedEvent;
    String jobCompletedEvent;

    public ScheduledTaskEventConfiguration(String schedulerName) {
        this.schedulerName = schedulerName;
        this.jobStartedEvent = schedulerName + "JobStarted";
        this.caseProcessedEvent = schedulerName + "CaseProcessed";
        this.caseFailedEvent = schedulerName + "CaseFailed";
        this.jobCompletedEvent = schedulerName + "JobCompleted";
    }
}
