package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HearingScheduledClaimantNotificationHandler.fillPreferredLocationData;

@Service
@RequiredArgsConstructor
public class HearingScheduledDefendantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT);
    public static final String TASK_ID = "GenerateDashboardNotificationHearingScheduledDefendant";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService toggleService;
    private final LocationReferenceDataService locationRefDataService;

    @Override
    protected Map<String, Callback> callbacks() {
        return toggleService.isCaseProgressionEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForHearingScheduled)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse configureScenarioForHearingScheduled(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getHearingCourtLocations(authToken));
        LocationRefData locationRefData = fillPreferredLocationData(locations, caseData.getHearingLocation());
        if (nonNull(locationRefData)) {
            caseData = caseData.toBuilder().hearingLocationCourtName(locationRefData.getSiteName()).build();
        }

        if (caseData.isRespondent1NotRepresented()) {
            dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                              SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT.getScenario(), authToken,
                                              ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
            );

            if (isNull(caseData.getTrialReadyRespondent1())) {
                dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                                  SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT.getScenario(), authToken,
                                                  ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
                );
            }

            dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                              SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario(), authToken,
                                              ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
