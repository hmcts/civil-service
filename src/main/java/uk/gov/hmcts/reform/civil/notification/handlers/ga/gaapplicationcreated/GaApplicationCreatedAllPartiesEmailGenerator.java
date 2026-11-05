package uk.gov.hmcts.reform.civil.notification.handlers.ga.gaapplicationcreated;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.AllGaPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import java.util.List;
import java.util.Set;

@Component
public class GaApplicationCreatedAllPartiesEmailGenerator extends AllGaPartiesEmailGenerator {

    private final GeneralAppFeesService feesService;

    public GaApplicationCreatedAllPartiesEmailGenerator(
        GaApplicationCreatedRespondentsEmailGenerator respondents,
        CaseDetailsConverter caseDetailsConverter,
        CoreCaseDataService coreCaseDataService,
        SolicitorEmailValidation solicitorEmailValidation,
        GeneralAppFeesService feesService) {
        super(List.of(respondents), caseDetailsConverter, coreCaseDataService, solicitorEmailValidation);
        this.feesService = feesService;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(GeneralApplicationCaseData caseData, String taskId) {
        if (!feesService.isFreeApplication(caseData) && caseData.getCcdState().equals(CaseState.PENDING_APPLICATION_ISSUED)) {
            return Set.of();
        }

        return super.getPartiesToNotify(caseData.copy().build(), taskId);
    }
}
