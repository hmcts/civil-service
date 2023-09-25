package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.ExpertReportTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExperts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQ;

import java.util.List;
import java.util.Optional;

public abstract class DQLipFormMapper {

    abstract protected List<HearingLipSupportRequirements> toHearingSupportRequirements(Optional<CaseDataLiP> caseDataLiPOptional);
    abstract protected DQExtraDetailsLip getDQExtraDetails(Optional<CaseDataLiP> caseDataLiPOptional);
    abstract protected String getStatementOfTruthName(CaseData caseData);

    public DirectionsQuestionnaireForm addLipDQs(DirectionsQuestionnaireForm form, Optional<CaseDataLiP> caseDataLiPOptional){
        var builder = form.toBuilder();
        builder.hearingLipSupportRequirements(toHearingSupportRequirements(caseDataLiPOptional));
        var dqExtraDetails = getDQExtraDetails(caseDataLiPOptional);
        if (dqExtraDetails != null) {
            builder.lipExtraDQ(LipExtraDQ.builder().triedToSettle(dqExtraDetails.getTriedToSettle())
                                   .requestExtra4weeks(dqExtraDetails.getRequestExtra4weeks())
                                   .considerClaimantDocuments(dqExtraDetails.getConsiderClaimantDocuments())
                                   .considerClaimantDocumentsDetails(dqExtraDetails.getConsiderClaimantDocumentsDetails())
                                   .determinationWithoutHearingRequired(dqExtraDetails.getDeterminationWithoutHearingRequired())
                                   .determinationWithoutHearingReason(dqExtraDetails.getDeterminationWithoutHearingReason())
                                   .giveEvidenceYourSelf(dqExtraDetails.getGiveEvidenceYourSelf())
                                   .whyPhoneOrVideoHearing(dqExtraDetails.getWhyPhoneOrVideoHearing())
                                   .wantPhoneOrVideoHearing(dqExtraDetails.getWantPhoneOrVideoHearing())
                                   .build())
                .lipExperts(LipExperts.builder()
                                .details(dqExtraDetails
                                             .getReportExpertDetails()
                                             .stream()
                                             .map(ExpertReportTemplate::toExpertReportTemplate)
                                             .toList())
                                .caseNeedsAnExpert(Optional.ofNullable(dqExtraDetails.getRespondent1DQLiPExpert())
                                                       .map(ExpertLiP::getCaseNeedsAnExpert).orElse(null))
                                .expertCanStillExamineDetails(Optional.ofNullable(dqExtraDetails.getRespondent1DQLiPExpert())
                                                                  .map(ExpertLiP::getExpertCanStillExamineDetails)
                                                                  .orElse(null))
                                .expertReportRequired(Optional.ofNullable(dqExtraDetails.getRespondent1DQLiPExpert())
                                                          .map(ExpertLiP::getExpertReportRequired)
                                                          .orElse(null))

                                .build());

        }
        return builder.build();
    }


}
