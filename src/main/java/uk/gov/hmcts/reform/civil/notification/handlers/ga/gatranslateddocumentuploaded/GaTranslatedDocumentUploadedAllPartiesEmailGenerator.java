package uk.gov.hmcts.reform.civil.notification.handlers.ga.gatranslateddocumentuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.AllGaPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Set;

@Component
public class GaTranslatedDocumentUploadedAllPartiesEmailGenerator extends AllGaPartiesEmailGenerator {

    public GaTranslatedDocumentUploadedAllPartiesEmailGenerator(
        GaTranslatedDocumentUploadedApplicantEmailGenerator applicant,
        GaTranslatedDocumentUploadedRespondentsEmailGenerator respondents,
        CaseDetailsConverter caseDetailsConverter,
        CoreCaseDataService coreCaseDataService,
        SolicitorEmailValidation solicitorEmailValidation) {
        super(List.of(applicant, respondents), caseDetailsConverter, coreCaseDataService, solicitorEmailValidation);
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(GeneralApplicationCaseData caseData, String taskId) {
        // Legacy translated-upload callbacks used refreshed addresses for sending but returned no case-data update.
        // Work on a copy so the shared aggregator does not introduce solicitor-detail persistence on this journey.
        return super.getPartiesToNotify(caseData.copy().build(), taskId);
    }
}
