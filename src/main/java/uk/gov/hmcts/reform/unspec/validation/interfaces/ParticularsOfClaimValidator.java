package uk.gov.hmcts.reform.unspec.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServedDocumentFiles;

import static java.util.Optional.ofNullable;

public interface ParticularsOfClaimValidator {

    private ServedDocumentFiles getServedDocumentFiles(CaseData caseData) {
        ServedDocumentFiles servedDocumentFiles = caseData.getServedDocumentFiles();

        return ofNullable(servedDocumentFiles)
           .orElse(ServedDocumentFiles.builder().build());
    }

    default CallbackResponse validateParticularsOfClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(caseData).getErrors())
            .build();
    }

    default CallbackResponse validateParticularsOfClaimAddOrAmendDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(caseData).getErrorsAddOrAmendDocuments())
            .build();
    }
}
