package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

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


}
