package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;

public record DirectionsOrderTaskContext(
    CaseData caseData,
    CallbackParams callbackParams,
    DirectionsOrderLifecycleStage stage
) {
}
