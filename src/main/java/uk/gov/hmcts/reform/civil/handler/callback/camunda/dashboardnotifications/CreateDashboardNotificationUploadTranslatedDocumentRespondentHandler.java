package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_TRANSLATED_DOC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT;

@Service
public class CreateDashboardNotificationUploadTranslatedDocumentRespondentHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_TRANSLATED_DOC);

    public CreateDashboardNotificationUploadTranslatedDocumentRespondentHandler(DashboardScenariosService dashboardScenariosService,
                                                                                DashboardNotificationsParamsMapper mapper,
                                                                                FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return (caseData.getIsGaRespondentOneLip() == YesOrNo.YES
            && ((caseData.getGeneralAppInformOtherParty() != null && caseData.getGeneralAppInformOtherParty().getIsWithNotice() == YES)
            || caseData.getGeneralAppConsentOrder() == YES));
    }
}
