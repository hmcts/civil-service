package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks;

public enum DirectionsOrderLifecycleStage {
    PRE_POPULATE,
    ORDER_DETAILS,
    MID_EVENT,
    DOCUMENT_GENERATION,
    SUBMISSION,
    CONFIRMATION
}
