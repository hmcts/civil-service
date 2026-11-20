package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class CounterClaimConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    private static final String TEXT =
        "<br>You've chosen to counterclaim - this means your defence cannot continue online."
        + " Use form N9B to counterclaim, do not create a new claim."
        + "<br><br><a href=\"https://www.gov.uk/respond-money-claim\" target=\"_blank\">Download form N9B (opens in a new tab)</a>";

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        if (!RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
            caseData.getCurrentDefendantClaimResponseTypeForSpec())) {
            return Optional.empty();
        }
        // Same-solicitor divergent responses are handled by SpecResponse1v2DivergentText
        if (isSameSolicitorDivergentResponse(caseData)) {
            return Optional.empty();
        }
        return Optional.of(TEXT);
    }

    private boolean isSameSolicitorDivergentResponse(CaseData caseData) {
        return NO.equals(caseData.getRespondentResponseIsSame())
            && ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null
            && caseData.getRespondent2ClaimResponseTypeForSpec() != null
            && !caseData.getRespondent1ClaimResponseTypeForSpec()
            .equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }
}
