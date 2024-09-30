package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGeneratorTasks.DQGeneratorFormBuilderTask;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGeneratorTasks.GetRespondentsForDQGeneratorTask;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGeneratorTasks.SetApplicantsForDQGeneratorTask;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LIP_RESPONSE;

@Service
public class DirectionQuestionnaireLipResponseGenerator extends DirectionsQuestionnaireGenerator {

    private static final DQLipFormMapperFactory MAPPER_FACTORY = new DQLipFormMapperFactory();

    public DirectionQuestionnaireLipResponseGenerator(DocumentManagementService documentManagementService,
                                                      DocumentGeneratorService documentGeneratorService,
                                                      IStateFlowEngine stateFlowEngine,
                                                      RepresentativeService representativeService,
                                                      FeatureToggleService featureToggleService,
                                                      LocationReferenceDataService locationRefDataService,
                                                      GetRespondentsForDQGeneratorTask respondentsForDQGeneratorTask,
                                                      SetApplicantsForDQGeneratorTask setApplicantsForDQGeneratorTask,
                                                      DQGeneratorFormBuilderTask dqGeneratorFormBuilderTask) {

        super(
            documentManagementService,
            documentGeneratorService,
            stateFlowEngine,
            representativeService,
            featureToggleService,
            locationRefDataService,
            respondentsForDQGeneratorTask,
            setApplicantsForDQGeneratorTask,
            dqGeneratorFormBuilderTask
        );
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = getDqGeneratorFormBuilderTask().getDirectionsQuestionnaireFormBuilder(
            caseData,
            authorisation
        );
        DQLipFormMapper mapper = MAPPER_FACTORY.getDQLipFormMapper(caseData);
        builder.lipStatementOfTruthName(mapper.getStatementOfTruthName(caseData))
            .applicant(Party.toLipParty(caseData.getApplicant1()))
            .respondent1LiPCorrespondenceAddress(caseData.getRespondent1CorrespondenceAddress())
            .allocatedTrack(caseData.getResponseClaimTrack())
            .fixedRecoverableCosts(mapper.getFixedRecoverableCostsIntermediate(caseData))
            .disclosureOfElectronicDocuments(mapper.getDisclosureOfElectronicDocuments(caseData))
            .disclosureOfNonElectronicDocuments(mapper.getDisclosureOfNonElectronicDocuments(caseData))
            .documentsToBeConsidered(mapper.getDocumentsToBeConsidered(caseData));
        return mapper.addLipDQs(builder.build(), Optional.ofNullable(caseData.getCaseDataLiP()));
    }

    @Override
    protected DocmosisTemplates getTemplateId(CaseData caseData) {
        if ((caseData.isRespondent1NotRepresented() || caseData.isApplicantNotRepresented())
            && getFeatureToggleService().isLipVLipEnabled()) {
            return DQ_LIP_RESPONSE;
        }
        return super.getTemplateId(caseData);
    }


    protected List<Party> getApplicants(CaseData caseData) {
        return List.of(Party.toLipParty(caseData.getApplicant1()));
    }

    protected List<Party> getRespondents(CaseData caseData, String defendantIdentifier) {
        return List.of(Party.toLipParty(caseData.getRespondent1()));
    }
}
