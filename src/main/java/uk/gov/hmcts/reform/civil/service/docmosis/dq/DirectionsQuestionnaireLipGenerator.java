package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.ExpertReportTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExperts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQ;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQEvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.builders.DQGeneratorFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements.toHearingSupportRequirements;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LR_V_LIP_RESPONSE;

@Service
public class DirectionsQuestionnaireLipGenerator extends DirectionsQuestionnaireGenerator {

    public DirectionsQuestionnaireLipGenerator(DocumentManagementService documentManagementService,
                                               DocumentGeneratorService documentGeneratorService,
                                               FeatureToggleService featureToggleService,
                                               DQGeneratorFormBuilder dqGeneratorFormBuilder,
                                               RespondentTemplateForDQGenerator respondentTemplateForDQGenerator
    ) {

        super(
            documentManagementService,
            documentGeneratorService,
            featureToggleService,
            dqGeneratorFormBuilder,
            respondentTemplateForDQGenerator
        );
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(
            caseData,
            authorisation
        );
        builder.respondent1LiPCorrespondenceAddress(caseData.getRespondent1CorrespondenceAddress())
            .hearingLipSupportRequirements(Optional.ofNullable(
                    caseData.getCaseDataLiP())
                .map(
                    CaseDataLiP::getRespondent1LiPResponse)
                .map(
                    RespondentLiPResponse::getRespondent1DQHearingSupportLip)
                .map(HearingSupportLip::getUnwrappedRequirementsLip)
                .map(Collection::stream)
                .map(items -> items.map(item -> toHearingSupportRequirements(item))
                    .toList())
                .orElse(Collections.emptyList()));
        var respondent1DQExtraDetails = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1DQExtraDetails)
            .orElse(null);
        if (respondent1DQExtraDetails != null) {
            builder.lipExtraDQ(LipExtraDQ.builder().triedToSettle(respondent1DQExtraDetails.getTriedToSettle())
                    .requestExtra4weeks(respondent1DQExtraDetails.getRequestExtra4weeks())
                    .considerClaimantDocuments(respondent1DQExtraDetails.getConsiderClaimantDocuments())
                    .considerClaimantDocumentsDetails(respondent1DQExtraDetails.getConsiderClaimantDocumentsDetails())
                    .determinationWithoutHearingRequired(respondent1DQExtraDetails.getDeterminationWithoutHearingRequired())
                    .determinationWithoutHearingReason(respondent1DQExtraDetails.getDeterminationWithoutHearingReason())
                    .giveEvidenceYourSelf(respondent1DQExtraDetails.getGiveEvidenceYourSelf())
                    .whyPhoneOrVideoHearing(respondent1DQExtraDetails.getWhyPhoneOrVideoHearing())
                    .wantPhoneOrVideoHearing(respondent1DQExtraDetails.getWantPhoneOrVideoHearing())
                    .giveEvidenceConfirmDetails(getDetails(caseData.getCaseDataLiP().getRespondent1LiPResponse()))
                    .build())
                .lipExperts(LipExperts.builder()
                    .details(respondent1DQExtraDetails
                        .getReportExpertDetails()
                        .stream()
                        .map(ExpertReportTemplate::toExpertReportTemplate)
                        .toList())
                    .caseNeedsAnExpert(Optional.ofNullable(respondent1DQExtraDetails.getRespondent1DQLiPExpert())
                        .map(ExpertLiP::getCaseNeedsAnExpert).orElse(null))
                    .expertCanStillExamineDetails(Optional.ofNullable(respondent1DQExtraDetails.getRespondent1DQLiPExpert())
                        .map(ExpertLiP::getExpertCanStillExamineDetails)
                        .orElse(null))
                    .expertReportRequired(Optional.ofNullable(respondent1DQExtraDetails.getRespondent1DQLiPExpert())
                        .map(ExpertLiP::getExpertReportRequired)
                        .orElse(null))

                    .build());

        }
        return builder.build();
    }

    @Override
    protected DocmosisTemplates getTemplateId(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented() && featureToggleService.isPinInPostEnabled()) {
            return DQ_LR_V_LIP_RESPONSE;
        }
        return super.getTemplateId(caseData);
    }

    protected List<Party> getRespondents(CaseData caseData, String defendantIdentifier) {
        return List.of(Party.builder()
            .name(caseData.getRespondent1().getPartyName())
            .emailAddress(caseData.getRespondent1().getPartyEmail())
            .phoneNumber(caseData.getRespondent1().getPartyPhone())
            .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
            .build());
    }

    protected RequestedCourt getRequestedCourt(DQ dq, String authorisation) {
        RequestedCourt rc = dq.getRequestedCourt();
        if (rc != null && null != rc.getCaseLocation()) {
            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(YES)
                .reasonForHearingAtSpecificCourt(rc.getReasonForHearingAtSpecificCourt())
                .responseCourtName(rc.getCaseLocation().getBaseLocation())
                .build();
        } else {
            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(NO)
                .build();
        }
    }

    private LipExtraDQEvidenceConfirmDetails getDetails(RespondentLiPResponse respondentLiPResponse) {
        EvidenceConfirmDetails confirmDetails = respondentLiPResponse.getRespondent1DQEvidenceConfirmDetails();
        if (confirmDetails != null) {
            return LipExtraDQEvidenceConfirmDetails.builder()
                .firstName(confirmDetails.getFirstName())
                .lastName(confirmDetails.getLastName())
                .email(confirmDetails.getEmail())
                .phone(confirmDetails.getPhone())
                .jobTitle(confirmDetails.getJobTitle())
                .build();
        }
        return null;
    }
}
