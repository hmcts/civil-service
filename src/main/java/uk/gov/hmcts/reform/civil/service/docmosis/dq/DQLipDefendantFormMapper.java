package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQEvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements.toHearingSupportRequirementsList;
import static uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQEvidenceConfirmDetails.toLipExtraDQEvidenceConfirmDetails;

public class DQLipDefendantFormMapper extends DQLipFormMapper {

    @Override
    protected List<HearingLipSupportRequirements> toHearingSupportRequirements(Optional<CaseDataLiP> caseDataLiPOptional) {
        Optional<HearingSupportLip> hearingLipSupportRequirements = caseDataLiPOptional
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1DQHearingSupportLip);
        return toHearingSupportRequirementsList(hearingLipSupportRequirements);
    }

    @Override
    protected LipExtraDQEvidenceConfirmDetails toEvidenceConfirmDetails(Optional<CaseDataLiP> caseDataLiPOptional) {
        Optional<EvidenceConfirmDetails> evidenceConfirmDetails = caseDataLiPOptional.map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1DQEvidenceConfirmDetails);
        return toLipExtraDQEvidenceConfirmDetails(evidenceConfirmDetails);
    }

    @Override
    protected DQExtraDetailsLip getDQExtraDetails(Optional<CaseDataLiP> caseDataLiPOptional) {
        return caseDataLiPOptional.map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1DQExtraDetails).orElse(null);
    }

    @Override
    protected Optional<ExpertLiP> getExpertLip(DQExtraDetailsLip dqExtraDetailsLip) {
        return Optional.ofNullable(dqExtraDetailsLip).map(DQExtraDetailsLip::getRespondent1DQLiPExpert);
    }

    @Override
    protected String getStatementOfTruthName(CaseData caseData) {
        return caseData.getRespondent1().getPartyName();
    }

    @Override
    protected FixedRecoverableCostsSection getFixedRecoverableCostsIntermediate(CaseData caseData) {
        return FixedRecoverableCostsSection.from(caseData.getRespondent1DQ().getRespondent1DQFixedRecoverableCostsIntermediate());
    }

    @Override
    protected DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments(CaseData caseData) {
        return caseData.getRespondent1DQ().getSpecRespondent1DQDisclosureOfElectronicDocuments();
    }

    @Override
    protected DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments(CaseData caseData) {
        return caseData.getRespondent1DQ().getSpecRespondent1DQDisclosureOfNonElectronicDocuments();
    }

    @Override
    protected DocumentsToBeConsideredSection getDocumentsToBeConsidered(CaseData caseData) {
        return DocumentsToBeConsideredSection.from(caseData.getRespondent1DQ().getRespondent1DQClaimantDocumentsToBeConsidered(), true);
    }
}
