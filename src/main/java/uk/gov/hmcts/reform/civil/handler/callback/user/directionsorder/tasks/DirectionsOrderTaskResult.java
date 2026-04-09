package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks;

import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;

public record DirectionsOrderTaskResult(
    CaseData updatedCaseData,
    List<String> errors,
    SubmittedCallbackResponse submittedCallbackResponse
) {

    public static DirectionsOrderTaskResult empty(CaseData caseData) {
        return new DirectionsOrderTaskResult(caseData, Collections.emptyList(), null);
    }

    public static DirectionsOrderTaskResult withErrors(CaseData caseData, List<String> errors) {
        return new DirectionsOrderTaskResult(caseData, errors, null);
    }

    public static DirectionsOrderTaskResult withSubmittedResponse(SubmittedCallbackResponse response) {
        return new DirectionsOrderTaskResult(null, Collections.emptyList(), response);
    }
}
