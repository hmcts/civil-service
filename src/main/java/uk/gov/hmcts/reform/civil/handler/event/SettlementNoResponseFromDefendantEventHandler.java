package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadRespondentNotificationHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementNoResponseFromDefendantEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;

    @EventListener
    public void sendEvidenceUploadNotification(SettlementNoResponseFromDefendantEvent event) {
        CaseDetails caseDetails = coreCaseDataService.getCase(event.caseId());
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        dashboardApiClient.recordScenario(
            caseData.getCcdCaseReference().toString(),
            DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_NO_RESPONSE_CLAIMANT.getScenario(),
            userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword()),
            ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
        );
    }
}
