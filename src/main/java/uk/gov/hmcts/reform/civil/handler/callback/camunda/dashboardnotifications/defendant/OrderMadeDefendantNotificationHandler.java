package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.OrderCallbackHandler;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_SDO_DRAWN_PRE_CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_WITHOUT_UPLOAD_FILES_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class OrderMadeDefendantNotificationHandler extends OrderCallbackHandler {

    private final ObjectMapper objectMapper;
    protected final WorkingDayIndicator workingDayIndicator;

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT);
    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderDefendant";
    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;

    public OrderMadeDefendantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper,
                                                 FeatureToggleService featureToggleService, ObjectMapper objectMapper,
                                                 WorkingDayIndicator workingDayIndicator,
                                                 DashboardNotificationService dashboardNotificationService,
                                                 TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService, workingDayIndicator);
        this.objectMapper = objectMapper;
        this.workingDayIndicator = workingDayIndicator;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
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
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (isNull(caseData.getRequestForReconsiderationDeadline())
            && isSDOEvent(callbackParams)
            && isEligibleForReconsideration(caseData)) {
            caseDataBuilder.requestForReconsiderationDeadline(getDateWithoutBankHolidays());
        }
        HashMap<String, Object> paramsMap = (HashMap<String, Object>) mapper.mapCaseDataToParams(caseDataBuilder.build(), caseEvent);

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData, callbackParams);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            dashboardScenariosService.recordScenarios(
                authToken,
                scenario,
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(paramsMap).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    @Override
    protected String getScenario(CaseData caseData, CallbackParams callbackParams) {
        if (isSDOEvent(callbackParams)
            && isEligibleForReconsideration(caseData)) {
            return SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DEFENDANT.getScenario();
        }
        if (isCarmApplicableCase(caseData)
            && isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(caseData)
            && isSDOEvent(callbackParams)
            && hasTrackChanged(caseData)) {

            if (hasUploadDocuments(caseData)) {
                return SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_CARM.getScenario();
            } else {
                return SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_WITHOUT_UPLOAD_FILES_CARM.getScenario();
            }
        }
        if (isSDODrawnPreCPRelease(caseData)) {
            return SCENARIO_AAA6_DEFENDANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario();
        }

        if (isFinalOrderIssued(callbackParams)) {
            deleteNotificationAndInactiveTasks(caseData);
            if (isOrderMadeFastTrackTrialNotResponded(caseData)) {
                return SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT.getScenario();
            }
            return SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario();
        }
        return SCENARIO_AAA6_CP_ORDER_MADE_DEFENDANT.getScenario();

    }

    private boolean hasUploadDocuments(CaseData caseData) {
        return !(isNull(caseData.getRes1MediationDocumentsReferred())
            && isNull(caseData.getRes1MediationNonAttendanceDocs())
            && isNull(caseData.getApp1MediationDocumentsReferred())
            && isNull(caseData.getApp1MediationNonAttendanceDocs()));
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
    }

    private boolean isFinalOrderIssued(CallbackParams callbackParams) {
        return All_FINAL_ORDERS_ISSUED.toString().equals(callbackParams.getRequest().getCaseDetails().getState());
    }

    private boolean isSDOEvent(CallbackParams callbackParams) {
        return CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT
            .equals(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }

    private boolean isSDODrawnPreCPRelease(CaseData caseData) {
        return !getFeatureToggleService()
            .isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation());
    }

    private boolean isOrderMadeFastTrackTrialNotResponded(CaseData caseData) {
        return SdoHelper.isFastTrack(caseData) && isNull(caseData.getTrialReadyRespondent1());
    }

    private void deleteNotificationAndInactiveTasks(CaseData caseData) {

        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );
    }
}
