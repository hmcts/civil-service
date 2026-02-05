package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks;

public interface DirectionsOrderCallbackTask {

    DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context);

    default boolean supports(DirectionsOrderLifecycleStage stage) {
        return true;
    }

    default boolean appliesTo(DirectionsOrderTaskContext context) {
        return true;
    }
}
