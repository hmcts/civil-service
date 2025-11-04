package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

public record SdoTaskContext(
    CaseData caseData,
    CallbackParams callbackParams,
    SdoLifecycleStage stage
) {
}
