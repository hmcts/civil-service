package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.Value;

@Value
public class ScheduledTaskEventConfiguration {

    public static final String SCHEDULER_JOB_STARTED = "SchedulerJobStarted";
    public static final String SCHEDULER_CASE_PROCESSED = "SchedulerCaseProcessed";
    public static final String SCHEDULER_CASE_FAILED = "SchedulerCaseFailed";
    public static final String SCHEDULER_CASE_ABORTED = "SchedulerCaseAborted";
    public static final String SCHEDULER_JOB_COMPLETED = "SchedulerJobCompleted";
    public static final String SCHEDULER_JOB_ABORTED = "SchedulerJobAborted";
    public static final String SCHEDULER_BACK_PRESSURE_UPDATED = "SchedulerBackPressureUpdated";

    String schedulerName;
}
