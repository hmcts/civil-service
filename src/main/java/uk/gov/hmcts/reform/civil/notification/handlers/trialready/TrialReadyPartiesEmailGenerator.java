package uk.gov.hmcts.reform.civil.notification.handlers.trialready;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_TWO;

@AllArgsConstructor
public abstract class TrialReadyPartiesEmailGenerator implements PartiesEmailGenerator {

    protected final TrialReadyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;
    protected final TrialReadyClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    protected final TrialReadyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;
    protected final TrialReadyRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;
    protected final TrialReadyDefendantEmailDTOGenerator defendantEmailDTOGenerator;
    protected final TrialReadyDefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator;
    protected final IStateFlowEngine stateFlowEngine;

    protected EmailDTO getApplicant(CaseData caseData) {
        if (caseData.getTrialReadyApplicant() != null) {
            return null;
        }

        boolean isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isApplicantNotRepresented();
        return isLiP ? claimantEmailDTOGenerator.buildEmailDTO(caseData) : appSolOneEmailDTOGenerator.buildEmailDTO(caseData);
    }

    protected EmailDTO getRespondentOne(CaseData caseData) {
        if (caseData.getTrialReadyRespondent1() != null) {
            return null;
        }

        boolean isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent1LiP();
        return isLiP ? defendantEmailDTOGenerator.buildEmailDTO(caseData) : respSolOneEmailDTOGenerator.buildEmailDTO(caseData);
    }

    protected EmailDTO getRespondentTwo(CaseData caseData) {
        boolean isTwoLipDefendants = stateFlowEngine.evaluate(caseData).isFlagSet(UNREPRESENTED_DEFENDANT_TWO);
        if (caseData.getTrialReadyRespondent2() != null || !(isOneVTwoTwoLegalRep(caseData) || isTwoLipDefendants)) {
            return null;
        }

        boolean isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent2LiP();
        return isLiP ? defendantTwoEmailDTOGenerator.buildEmailDTO(caseData) : respSolTwoEmailDTOGenerator.buildEmailDTO(caseData);
    }
}
