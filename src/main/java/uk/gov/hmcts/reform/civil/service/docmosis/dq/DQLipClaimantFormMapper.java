package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements.toHearingSupportRequirementsList;

public class DQLipClaimantFormMapper extends DQLipFormMapper{

    @Override
    protected List<HearingLipSupportRequirements> toHearingSupportRequirements(Optional<CaseDataLiP> caseDataLiPOptional) {
        Optional<HearingSupportLip> hearingLipSupportRequirements = caseDataLiPOptional
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .map(ClaimantLiPResponse::getApplicant1DQHearingSupportLip);
        return toHearingSupportRequirementsList(hearingLipSupportRequirements);
    }

    @Override
    protected DQExtraDetailsLip getDQExtraDetails(Optional<CaseDataLiP> caseDataLiPOptional) {
        return caseDataLiPOptional
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .map(ClaimantLiPResponse::getApplicant1DQExtraDetails)
            .orElse(null);
    }

    @Override
    protected Optional<ExpertLiP> getExpertLip(DQExtraDetailsLip dqExtraDetailsLip) {
        return Optional.ofNullable(dqExtraDetailsLip).map(DQExtraDetailsLip::getApplicant1DQLiPExpert);
    }

    @Override
    protected String getStatementOfTruthName(CaseData caseData) {
        return caseData.getApplicant1().getPartyName();
    }
}
