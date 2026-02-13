package uk.gov.hmcts.reform.civil.service.dashboardnotifications.citizenhearingfeepayment;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_PAID_CLAIMANT;

@Service
public class CitizenHearingFeePaymentDashboardService extends DashboardScenarioService {

    public CitizenHearingFeePaymentDashboardService(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyCitizenHearingFeePayment(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if ((nonNull(caseData.getHearingFeePaymentDetails()) && caseData.getHearingFeePaymentDetails().getStatus() == SUCCESS)
            || (caseData.isHWFTypeHearing() && caseData.hearingFeeFullRemissionNotGrantedHWF())) {
            return SCENARIO_AAA6_HEARING_FEE_PAID_CLAIMANT.getScenario();
        }
        return null;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }
}
