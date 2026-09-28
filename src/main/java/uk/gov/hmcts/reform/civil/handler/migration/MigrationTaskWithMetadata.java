package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;

abstract class MigrationTaskWithMetadata<T extends CaseReference> extends MigrationTask<T> {

    private final String taskName;
    private final String eventSummary;
    private final String eventDescription;

    protected MigrationTaskWithMetadata(
        Class<T> type,
        String taskName,
        String eventSummary,
        String eventDescription
    ) {
        super(type);
        this.taskName = taskName;
        this.eventSummary = eventSummary;
        this.eventDescription = eventDescription;
    }

    @Override
    protected final String getTaskName() {
        return taskName;
    }

    @Override
    protected final String getEventSummary() {
        return eventSummary;
    }

    @Override
    protected final String getEventDescription() {
        return eventDescription;
    }
}
