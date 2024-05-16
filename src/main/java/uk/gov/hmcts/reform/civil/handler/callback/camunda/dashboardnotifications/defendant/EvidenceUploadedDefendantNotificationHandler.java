package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class EvidenceUploadedDefendantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT);
    private static final String TASK_ID = "GenerateDashboardNotificationEvidenceUploadedDefendant";

    public EvidenceUploadedDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        if (nonNull(caseData.getCaseDocumentUploadDateRes())) {
            return DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_DEFENDANT_UPLOADED_DEFENDANT.getScenario();
        } else {
            return DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_CLAIMANT_UPLOADED_DEFENDANT.getScenario();
        }
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }
}
