package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LR_V_LIP_RESPONSE;

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
        builder.dqExtraDetailsLip(Optional.ofNullable(caseData.getCaseDataLiP())
                                      .map(CaseDataLiP::getRespondent1LiPResponse)
                                      .map(RespondentLiPResponse::getRespondent1DQExtraDetails)
                                      .orElse(null))
            .respondent1LiPCorrespondenceAddress(Optional.ofNullable(caseData.getCaseDataLiP())
                                                     .map(CaseDataLiP::getRespondent1LiPResponse)
                                                     .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
                                                     .orElse(null));

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
