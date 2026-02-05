package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public record DirectionsOrderStageExecutionResult(
    CaseData caseData,
    List<String> errors
) {
}
