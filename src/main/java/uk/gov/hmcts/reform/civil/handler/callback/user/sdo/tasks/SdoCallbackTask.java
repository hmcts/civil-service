package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks;

public interface SdoCallbackTask {

    SdoTaskResult execute(SdoTaskContext context);

    default boolean supports(SdoLifecycleStage stage) {
        return true;
    }
}
