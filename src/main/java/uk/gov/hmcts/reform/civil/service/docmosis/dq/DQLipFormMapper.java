package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExperts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQ;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQEvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.docmosis.dq.ExpertReportTemplate.toExpertReportTemplate;

public abstract class DQLipFormMapper {

    protected abstract List<HearingLipSupportRequirements> toHearingSupportRequirements(Optional<CaseDataLiP> caseDataLiPOptional);

    protected abstract LipExtraDQEvidenceConfirmDetails toEvidenceConfirmDetails(Optional<CaseDataLiP> caseDataLiPOptional);

    protected abstract DQExtraDetailsLip getDQExtraDetails(Optional<CaseDataLiP> caseDataLiPOptional);

    protected abstract Optional<ExpertLiP> getExpertLip(DQExtraDetailsLip dqExtraDetailsLip);

    protected abstract String getStatementOfTruthName(CaseData caseData);

    protected abstract FixedRecoverableCostsSection getFixedRecoverableCostsIntermediate(CaseData caseData);

    protected abstract DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments(CaseData caseData);

    protected abstract DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments(CaseData caseData);

    protected abstract DocumentsToBeConsideredSection getDocumentsToBeConsidered(CaseData caseData);

    public DirectionsQuestionnaireForm addLipDQs(DirectionsQuestionnaireForm form, Optional<CaseDataLiP> caseDataLiPOptional) {
        var updatedForm = form.copy();
        updatedForm.setHearingLipSupportRequirements(toHearingSupportRequirements(caseDataLiPOptional));
        var dqExtraDetails = getDQExtraDetails(caseDataLiPOptional);
        var expertLip = getExpertLip(dqExtraDetails);
        var lipExtraDQEvidenceConfirmDetails = toEvidenceConfirmDetails(caseDataLiPOptional);
        if (dqExtraDetails != null) {
            updatedForm.setLipExtraDQ(new LipExtraDQ()
                                   .setTriedToSettle(dqExtraDetails.getTriedToSettle())
                                   .setRequestExtra4weeks(dqExtraDetails.getRequestExtra4weeks())
                                   .setConsiderClaimantDocuments(dqExtraDetails.getConsiderClaimantDocuments())
                                   .setConsiderClaimantDocumentsDetails(dqExtraDetails.getConsiderClaimantDocumentsDetails())
                                   .setDeterminationWithoutHearingRequired(dqExtraDetails.getDeterminationWithoutHearingRequired())
                                   .setDeterminationWithoutHearingReason(dqExtraDetails.getDeterminationWithoutHearingReason())
                                   .setGiveEvidenceYourSelf(dqExtraDetails.getGiveEvidenceYourSelf())
                                   .setWhyPhoneOrVideoHearing(dqExtraDetails.getWhyPhoneOrVideoHearing())
                                   .setWantPhoneOrVideoHearing(dqExtraDetails.getWantPhoneOrVideoHearing())
                                   .setGiveEvidenceConfirmDetails(lipExtraDQEvidenceConfirmDetails))
                .setLipExperts(new LipExperts()
                                .setDetails(expertLip.map(ExpertLiP::getUnwrappedDetails).map(Collection::stream)
                                             .map(stream -> stream.map(item -> toExpertReportTemplate(item)).toList())
                                             .orElse(Collections.emptyList()))
                                .setCaseNeedsAnExpert(expertLip
                                                       .map(ExpertLiP::getCaseNeedsAnExpert).orElse(null))
                                .setExpertCanStillExamineDetails(expertLip
                                                                  .map(ExpertLiP::getExpertCanStillExamineDetails)
                                                                  .orElse(null))
                                .setExpertReportRequired(expertLip
                                                          .map(ExpertLiP::getExpertReportRequired)
                                                          .orElse(null)));

        }
        return updatedForm;
    }
}
