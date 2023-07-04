package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

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
    protected DocmosisTemplates getTemplateId(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented() && getFeatureToggleService().isPinInPostEnabled()){
            return DQ_LR_V_LIP_RESPONSE;
        }
        return super.getTemplateId(caseData);
    }


}
