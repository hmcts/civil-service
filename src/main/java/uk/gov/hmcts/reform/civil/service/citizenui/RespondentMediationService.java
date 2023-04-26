package uk.gov.hmcts.reform.civil.service.citizenui;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Component
public class RespondentMediationService {

    public DefendantResponseShowTag setMediationRequired(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            if (multiPartyScenario.equals(ONE_V_ONE)) {
                return getDefendantResponseShowTagFor1v1(caseData);
            } else if (caseData.isMultiPartyClaimant(multiPartyScenario)) {
                return DefendantResponseShowTag.CLAIMANT_MEDIATION_TWO_V_ONE;
            } else {
                if (caseData.isMultiPartyDefendant()) {
                    return DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_TWO;
                }
            }
        }
        return null;
    }

    private DefendantResponseShowTag getDefendantResponseShowTagFor1v1(CaseData caseData) {
        switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
            case FULL_DEFENCE:
                if (caseData.isFullDefence() && caseData.hasDefendantAgreedToFreeMediation()) {
                    return DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE;
                }
                break;
            case PART_ADMISSION:
                if (caseData.hasDefendantAgreedToFreeMediation()) {
                    if (caseData.hasDefendantNotPaid()) {
                        return DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE;
                    } else if (caseData.isSettlementDeclinedByClaimant()) {
                        return DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE;
                    } else if (caseData.isClaimantRejectsClaimAmount()) {
                        return DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE;
                    }
                }
                break;
            case FULL_ADMISSION:
                if (caseData.isMultiPartyDefendant()) {
                    return DefendantResponseShowTag.CLAIMANT_MEDIATION_ADMIT_PAID_ONE_V_ONE;
                }
                break;
            default:
                return null;
        }

        return null;
    }

}
