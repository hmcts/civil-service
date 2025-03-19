package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.builders.DQGeneratorFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LIP_RESPONSE;

@Service
public class DirectionQuestionnaireLipResponseGenerator extends DirectionsQuestionnaireGenerator {

    private static final DQLipFormMapperFactory MAPPER_FACTORY = new DQLipFormMapperFactory();

    public DirectionQuestionnaireLipResponseGenerator(DocumentManagementService documentManagementService,
                                                      DocumentGeneratorService documentGeneratorService,
                                                      FeatureToggleService featureToggleService,
                                                      DQGeneratorFormBuilder dqGeneratorFormBuilder,
                                                      RespondentTemplateForDQGenerator respondentTemplateForDQGenerator) {

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
        if ((caseData.isRespondent1NotRepresented() || caseData.isApplicantNotRepresented())) {
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
