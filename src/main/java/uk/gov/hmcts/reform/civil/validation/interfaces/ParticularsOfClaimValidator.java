package uk.gov.hmcts.reform.civil.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;

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

    default CallbackResponse validateParticularsOfClaimBackwardsCompatible(CallbackParams callbackParams) {
        ServedDocumentFiles servedDocumentFiles = ofNullable(callbackParams.getCaseData().getServedDocumentFiles())
            .orElse(ServedDocumentFiles.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(servedDocumentFiles.getErrorsBackwardsCompatible())
            .build();
    }

    default CallbackResponse validateParticularsOfClaimAddOrAmendDocumentsBackwardsCompatible(CallbackParams
                                                                                                  callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(getServedDocumentFiles(caseData).getErrorsAddOrAmendDocumentsBackwardsCompatible())
            .build();
    }
}
