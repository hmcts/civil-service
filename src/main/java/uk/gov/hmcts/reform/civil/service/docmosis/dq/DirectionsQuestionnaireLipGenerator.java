package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.ExpertReportTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExperts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQ;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LR_V_LIP_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Service
public class DirectionsQuestionnaireLipGenerator extends DirectionsQuestionnaireGenerator {

    public DirectionsQuestionnaireLipGenerator(DocumentManagementService documentManagementService,
                                               DocumentGeneratorService documentGeneratorService,
                                               StateFlowEngine stateFlowEngine,
                                               RepresentativeService representativeService,
                                               FeatureToggleService featureToggleService,
                                               LocationRefDataService locationRefDataService) {
        super(
            documentManagementService,
            documentGeneratorService,
            stateFlowEngine,
            representativeService,
            featureToggleService,
            locationRefDataService
        );
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = getDirectionsQuestionnaireFormBuilder(
            caseData,
            authorisation
        );
        builder.respondent1LiPCorrespondenceAddress(Optional.ofNullable(caseData.getCaseDataLiP())
                                                          .map(CaseDataLiP::getRespondent1LiPResponse)
                                                          .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
                                                          .orElse(null));
        var respondent1DQExtraDetails = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1DQExtraDetails)
            .orElse(null);
        if(respondent1DQExtraDetails != null) {

            builder.lipExtraDQ(LipExtraDQ.builder().triedToSettle(respondent1DQExtraDetails.getTriedToSettle())
                                   .requestExtra4weeks(respondent1DQExtraDetails.getRequestExtra4weeks())
                                   .considerClaimantDocumentsDetails(respondent1DQExtraDetails.getConsiderClaimantDocumentsDetails())
                                   .determinationWithoutHearingReason(respondent1DQExtraDetails.getDeterminationWithoutHearingReason())
                                   .giveEvidenceYourSelf(respondent1DQExtraDetails.getGiveEvidenceYourSelf())
                                   .whyPhoneOrVideoHearing(respondent1DQExtraDetails.getWhyPhoneOrVideoHearing())
                                   .wantPhoneOrVideoHearing(respondent1DQExtraDetails.getWantPhoneOrVideoHearing())
                                   .build())
                    .lipExperts(LipExperts.builder()
                                        .details(respondent1DQExtraDetails
                                                     .getReportExpertDetails()
                                                     .stream()
                                                     .map(detail -> ExpertReportTemplate.toExpertReportTemplate(detail))
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
        if (caseData.isRespondent1NotRepresented() && getFeatureToggleService().isPinInPostEnabled()){
            return DQ_LR_V_LIP_RESPONSE;
        }
        return super.getTemplateId(caseData);
    }

    @Override
    protected List<Party> getRespondents(CaseData caseData, String defendantIdentifier) {
        return List.of(Party.builder()
                            .name(caseData.getRespondent1().getPartyName())
                            .emailAddress(caseData.getRespondent1().getPartyEmail())
                            .phoneNumber(caseData.getRespondent1().getPartyPhone())
                            .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                            .build());
    }

}
