package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RespondToDefenceSpecCallbackHandler.DOWNLOAD_URL_CLAIM_DOCUMENTS;

@Component
public class JudgmentByAdmissionConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        if (CaseState.All_FINAL_ORDERS_ISSUED == caseData.getCcdState()
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())) {
            return Optional.of(format(
                "<br />%n%n<a href=\"%s\" target=\"_blank\">Download county court judgment</a>"
                    + "<br><br>The defendant will be served the county court judgment<br><br>",
                format(DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference())
            ));
        }
        return Optional.empty();
    }
}
