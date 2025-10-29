package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementHandler.checkLocation;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isEvent;

@Service
@RequiredArgsConstructor
public class HearingScheduledClaimantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC);
    public static final String TASK_ID = "GenerateDashboardNotificationHearingScheduledClaimant";
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final LocationReferenceDataService locationRefDataService;
    private final HearingNoticeCamundaService camundaService;
    private final HearingFeesService hearingFeesService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForHearingScheduled);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse configureScenarioForHearingScheduled(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        final String caseId = String.valueOf(caseData.getCcdCaseReference());
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
                .getHearingCourtLocations(authToken));
        LocationRefData locationRefData = fillPreferredLocationData(locations, caseData.getHearingLocation());
        if (nonNull(locationRefData)) {
            caseData = caseData.toBuilder().hearingLocationCourtName(locationRefData.getSiteName()).build();
        }

        if (caseData.isApplicant1NotRepresented()) {
            dashboardScenariosService.recordScenarios(
                    authToken,
                    SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(), caseId,
                    ScenarioRequestParams.builder().params(
                            mapper.mapCaseDataToParams(caseData)).build()
            );
        }

        boolean isAutoHearingNotice = isEvent(callbackParams, CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC);
        boolean hasPaidFee = (caseData.getHearingFeePaymentDetails() != null
            && SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus())) || caseData.hearingFeePaymentDoneWithHWF();

        HearingNoticeVariables camundaVars = isAutoHearingNotice
            ? camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId())
            : null;

        if (isAutoHearingNotice && hearingFeeRequired(camundaVars.getHearingType()) && !hasPaidFee) {
            caseData.setHearingFee(HearingFeeUtils.calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack()));
        }

        boolean shouldRecordFeeScenario = caseData.isApplicant1NotRepresented() && !hasPaidFee
            && ((!isAutoHearingNotice && caseData.getCcdState() == HEARING_READINESS && caseData.getListingOrRelisting() == LISTING)
                || (isAutoHearingNotice && hearingFeeRequired(camundaVars.getHearingType())));

        if (shouldRecordFeeScenario) {
            dashboardScenariosService.recordScenarios(authToken,
                                              SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(), caseId,
                                              ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }

        if (caseData.isApplicant1NotRepresented()) {
            if (AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack())
                && isNull(caseData.getTrialReadyApplicant())) {
                dashboardScenariosService.recordScenarios(authToken,
                                                  SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT.getScenario(), caseId,
                                                  ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
                );
            }

            dashboardScenariosService.recordScenarios(authToken,
                                              SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(), caseId,
                                              ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public static LocationRefData fillPreferredLocationData(final List<LocationRefData> locations,
                                                      DynamicList hearingLocation) {
        if (locations.isEmpty() || isNull(hearingLocation)) {
            return null;
        }
        String locationLabel = hearingLocation.getValue().getLabel();
        var preferredLocation =
            locations
                .stream()
                .filter(locationRefData -> checkLocation(
                    locationRefData,
                    locationLabel
                )).findFirst();
        return preferredLocation.orElse(null);
    }
}
