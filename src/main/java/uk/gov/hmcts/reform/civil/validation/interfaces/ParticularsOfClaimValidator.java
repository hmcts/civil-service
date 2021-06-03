package uk.gov.hmcts.reform.civil.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;

import static java.util.Optional.ofNullable;

public interface ParticularsOfClaimValidator {

    private ServedDocumentFiles getServedDocumentFiles(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return ofNullable(caseData.getServedDocumentFiles())
           .orElse(ServedDocumentFiles.builder().build());
    }

    default CallbackResponse validateParticularsOfClaim(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(callbackParams).getErrors())
            .build();
    }

    default CallbackResponse validateParticularsOfClaimAddOrAmendDocuments(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(callbackParams).getErrorsAddOrAmendDocuments())
            .build();
    }

    default CallbackResponse validateParticularsOfClaimBackwardsCompatible(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(callbackParams).getErrorsBackwardsCompatible())
            .build();
    }

    default CallbackResponse validateParticularsOfClaimAddOrAmendDocumentsBackwardsCompatible(CallbackParams
                                                                                                  callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(callbackParams).getErrorsAddOrAmendDocumentsBackwardsCompatible())
            .build();
    }
}
