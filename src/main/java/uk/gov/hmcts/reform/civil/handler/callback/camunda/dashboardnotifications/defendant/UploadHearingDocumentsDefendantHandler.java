package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Collections;
import java.util.List;

@Service
public class UploadHearingDocumentsDefendantHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_DEFENDANT);
    private static final String TASK_ID = "CreateUploadHearingDocumentNotificationForDefendant";

    public UploadHearingDocumentsDefendantHandler(DashboardApiClient dashboardApiClient,
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
        return DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        // TODO check for Small Claims is because Small Claims does not use a known date
        return caseData.isRespondent1NotRepresented()
            && !SdoHelper.isSmallClaimsTrack(caseData)
            && featureToggleService.isCaseProgressionEnabled();
    }
}
