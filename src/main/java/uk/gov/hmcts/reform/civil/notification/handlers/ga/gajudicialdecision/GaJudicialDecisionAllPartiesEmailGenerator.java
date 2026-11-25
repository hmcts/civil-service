package uk.gov.hmcts.reform.civil.notification.handlers.ga.gajudicialdecision;

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
public class GaJudicialDecisionAllPartiesEmailGenerator extends AllGaPartiesEmailGenerator {

    private final GaJudicialDecisionEmailHelper helper;

    public GaJudicialDecisionAllPartiesEmailGenerator(
        GaJudicialDecisionApplicantEmailGenerator applicant,
        GaJudicialDecisionRespondentsEmailGenerator respondents,
        CaseDetailsConverter caseDetailsConverter,
        CoreCaseDataService coreCaseDataService,
        SolicitorEmailValidation solicitorEmailValidation,
        GaJudicialDecisionEmailHelper helper) {
        super(List.of(applicant, respondents), caseDetailsConverter, coreCaseDataService, solicitorEmailValidation);
        this.helper = helper;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(GeneralApplicationCaseData caseData, String taskId) {
        helper.prepareCase(caseData);
        return super.getPartiesToNotify(caseData, taskId);
    }
}
