package uk.gov.hmcts.reform.unspec.validation.interfaces;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.model.ServedDocumentFiles;

import static java.util.Optional.ofNullable;

public interface ParticularsOfClaimValidator {

    private ServedDocumentFiles getServedDocumentFiles(CallbackParams callbackParams) {

        return ofNullable(callbackParams.getCaseData().getServedDocumentFiles())
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
}
