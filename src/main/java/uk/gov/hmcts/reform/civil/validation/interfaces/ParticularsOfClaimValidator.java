package uk.gov.hmcts.reform.civil.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;

import static java.util.Optional.ofNullable;

public interface ParticularsOfClaimValidator {

    default ServedDocumentFiles getServedDocumentFiles(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return ofNullable(caseData.getServedDocumentFiles())
            .orElse(ServedDocumentFiles.builder().build());
    }

    default CallbackResponse validateParticularsOfClaim(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(callbackParams).getErrors())
            .build();
    }
}
