package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        DirectionsQuestionnaireForm form = dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(
            caseData,
            authorisation
        );
        form.setRespondent1LiPCorrespondenceAddress(caseData.getRespondent1CorrespondenceAddress())
            .setHearingLipSupportRequirements(Optional.ofNullable(
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
            form.setLipExtraDQ(new LipExtraDQ()
                    .setTriedToSettle(respondent1DQExtraDetails.getTriedToSettle())
                    .setRequestExtra4weeks(respondent1DQExtraDetails.getRequestExtra4weeks())
                    .setConsiderClaimantDocuments(respondent1DQExtraDetails.getConsiderClaimantDocuments())
                    .setConsiderClaimantDocumentsDetails(respondent1DQExtraDetails.getConsiderClaimantDocumentsDetails())
                    .setDeterminationWithoutHearingRequired(respondent1DQExtraDetails.getDeterminationWithoutHearingRequired())
                    .setDeterminationWithoutHearingReason(respondent1DQExtraDetails.getDeterminationWithoutHearingReason())
                    .setGiveEvidenceYourSelf(respondent1DQExtraDetails.getGiveEvidenceYourSelf())
                    .setWhyPhoneOrVideoHearing(respondent1DQExtraDetails.getWhyPhoneOrVideoHearing())
                    .setWantPhoneOrVideoHearing(respondent1DQExtraDetails.getWantPhoneOrVideoHearing())
                    .setGiveEvidenceConfirmDetails(getDetails(caseData.getCaseDataLiP().getRespondent1LiPResponse())))
                .setLipExperts(new LipExperts()
                    .setDetails(respondent1DQExtraDetails
                        .getReportExpertDetails()
                        .stream()
                        .map(ExpertReportTemplate::toExpertReportTemplate)
                        .toList())
                    .setCaseNeedsAnExpert(Optional.ofNullable(respondent1DQExtraDetails.getRespondent1DQLiPExpert())
                        .map(ExpertLiP::getCaseNeedsAnExpert).orElse(null))
                    .setExpertCanStillExamineDetails(Optional.ofNullable(respondent1DQExtraDetails.getRespondent1DQLiPExpert())
                        .map(ExpertLiP::getExpertCanStillExamineDetails)
                        .orElse(null))
                    .setExpertReportRequired(Optional.ofNullable(respondent1DQExtraDetails.getRespondent1DQLiPExpert())
                        .map(ExpertLiP::getExpertReportRequired)
                        .orElse(null)));

        }
        return form;
    }

    @Override
    protected DocmosisTemplates getTemplateId(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            final DocmosisTemplates dqLrVLipResponse = DQ_LR_V_LIP_RESPONSE;
            log.info("{} {}", caseData.getCcdCaseReference(), dqLrVLipResponse.getTemplate());
            return dqLrVLipResponse;
        }
        return super.getTemplateId(caseData);
    }

    protected List<Party> getRespondents(CaseData caseData, String defendantIdentifier) {
        return List.of(new Party()
            .setName(caseData.getRespondent1().getPartyName())
            .setEmailAddress(caseData.getRespondent1().getPartyEmail())
            .setPhoneNumber(caseData.getRespondent1().getPartyPhone())
            .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress()));
    }

    protected RequestedCourt getRequestedCourt(DQ dq, String authorisation) {
        RequestedCourt rc = dq.getRequestedCourt();
        if (rc != null && null != rc.getCaseLocation()) {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setRequestHearingAtSpecificCourt(YES);
            requestedCourt.setReasonForHearingAtSpecificCourt(rc.getReasonForHearingAtSpecificCourt());
            requestedCourt.setResponseCourtName(rc.getCaseLocation().getBaseLocation());
            return requestedCourt;
        } else {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setRequestHearingAtSpecificCourt(NO);
            return requestedCourt;
        }
    }

    private LipExtraDQEvidenceConfirmDetails getDetails(RespondentLiPResponse respondentLiPResponse) {
        EvidenceConfirmDetails confirmDetails = respondentLiPResponse.getRespondent1DQEvidenceConfirmDetails();
        if (confirmDetails != null) {
            return new LipExtraDQEvidenceConfirmDetails()
                .setFirstName(confirmDetails.getFirstName())
                .setLastName(confirmDetails.getLastName())
                .setEmail(confirmDetails.getEmail())
                .setPhone(confirmDetails.getPhone())
                .setJobTitle(confirmDetails.getJobTitle());
        }
        return null;
    }
}
