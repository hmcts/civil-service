package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_PROCESSED_DEFENDANT;

@Service
public class CoscCertificateGeneratedDefendantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT);
    private static final String TASK_ID = "GenerateDashboardNotificationCoSCCertificateGenerated";
    private static final String CONFIRM_CCJ_PAID = "Confirm you've paid a judgment debt";
    private static final String AWAITING_APPLICATION_PAYMENT = "Awaiting Application Payment";

    public CoscCertificateGeneratedDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                                DashboardNotificationsParamsMapper mapper,
                                                                FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_PROCESSED_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (caseData.getRespondentSolGaAppDetails() != null && !caseData.getRespondentSolGaAppDetails().isEmpty()) {
            List<Element<GADetailsRespondentSol>> app = caseData.getRespondentSolGaAppDetails().stream()
                .filter(gaApp -> gaApp.getValue().getGeneralApplicationType().equals(CONFIRM_CCJ_PAID)
                    && !Objects.equals(gaApp.getValue().getCaseState(), AWAITING_APPLICATION_PAYMENT)).toList();

            app.stream().forEach(gaApp -> dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(
                gaApp.getValue().getCaseLink().getCaseReference(),
                "APPLICANT",
                authToken
            ));
        }
    }
}
