package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks;

import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;

public record SdoTaskResult(
    CaseData updatedCaseData,
    List<String> errors,
    SubmittedCallbackResponse submittedCallbackResponse
) {

    public static SdoTaskResult empty(CaseData caseData) {
        return new SdoTaskResult(caseData, Collections.emptyList(), null);
    }

    public static SdoTaskResult withErrors(CaseData caseData, List<String> errors) {
        return new SdoTaskResult(caseData, errors, null);
    }

    public static SdoTaskResult withSubmittedResponse(SubmittedCallbackResponse response) {
        return new SdoTaskResult(null, Collections.emptyList(), response);
    }
}
