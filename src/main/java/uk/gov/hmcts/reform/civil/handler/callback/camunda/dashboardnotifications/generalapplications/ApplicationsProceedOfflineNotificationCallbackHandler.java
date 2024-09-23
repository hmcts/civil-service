package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.generalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT;

@Service
@RequiredArgsConstructor
public class ApplicationsProceedOfflineNotificationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_APPLICANT,
                CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_RESPONDENT);
    public static final String TASK_ID = "applicantLipApplicationOfflineDashboardNotification";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final String CLAIMANT = "CLAIMANT";
    private final String DEFENDANT = "DEFENDANT";

    private static final List<String> NON_LIVE_STATES = List.of(
        "APPLICATION_CLOSED",
        "PROCEEDS_IN_HERITAGE",
        "ORDER_MADE",
        "APPLICATION_DISMISSED"
    );

    private static final List<String> WITHOUT_NOTICE_STATES = List.of(
        "APPLICATION_ADD_PAYMENT"
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isLipVLipEnabled()
            && featureToggleService.isGeneralApplicationsEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForProceedOffline)
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

    private CallbackResponse configureScenarioForProceedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!caseData.getCcdState().equals(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            || caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String notificationType = notificationType(callbackParams);
        if (notificationType == null) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        if (getLatestStatusOfGeneralApplication(caseData, notificationType)) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                notificationType.equals(CLAIMANT)
                    ? SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT.getScenario()
                    : SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT.getScenario(),
                authToken,
                ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    // Check is live
    private boolean isLive(String applicationState) {
        return !NON_LIVE_STATES.contains(applicationState);
    }

    // Check is Claimant
    private boolean isClaimantApplicant(GeneralApplication application) {
        return YesOrNo.YES.equals(application.getParentClaimantIsApplicant());
    }

    private String notificationType(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String eventId = callbackParams.getRequest().getEventId();
        if (eventId.equals(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_APPLICANT.name())
            && caseData.isApplicantLiP()) {
            return CLAIMANT;
        } else if (eventId.equals(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_RESPONDENT.name())
            && caseData.isRespondent1LiP()) {
            return DEFENDANT;
        }
        return null;
    }

    // Get all applications
    private boolean getLatestStatusOfGeneralApplication(CaseData caseData, String notificationType) {
        for (Element<GeneralApplication> element : caseData.getGeneralApplications()) {
            Long caseReference = parseLong(element.getValue().getCaseLink().getCaseReference());
            GeneralApplication application = caseDetailsConverter.toGeneralApplication(coreCaseDataService
                                                                                           .getCase(caseReference));
            System.out.println(application.getGeneralApplicationState());
            System.out.println(application);
            if (notificationType.equals(CLAIMANT)
                && (isClaimantApplicantApplicationValid(application)
                || isClaimantRespondentApplicationValid(application))) {
                return true;
            }
            if (notificationType.equals(DEFENDANT)
                && (isDefendantApplicantApplicationValid(application)
                || isDefendantRespondentApplicationValid(application))) {
                return true;
            }
        }
        return false;
    }

    private boolean isClaimantApplicantApplicationValid(GeneralApplication application) {
        return isLive(application.getGeneralApplicationState())
            && isClaimantApplicant(application);
    }

    private boolean isClaimantRespondentApplicationValid(GeneralApplication application) {
        return isLive(application.getGeneralApplicationState())
            && !isClaimantApplicant(application)
            && (YesOrNo.YES == application.getGeneralAppInformOtherParty().getIsWithNotice()
            || isWithoutNoticeToWithNotice(application));
    }

    private boolean isDefendantApplicantApplicationValid(GeneralApplication application) {
        return isLive(application.getGeneralApplicationState())
            && !isClaimantApplicant(application);
    }

    private boolean isDefendantRespondentApplicationValid(GeneralApplication application) {
        return isLive(application.getGeneralApplicationState())
            && isClaimantApplicant(application)
            && (YesOrNo.YES == application.getGeneralAppInformOtherParty().getIsWithNotice()
            || isWithoutNoticeToWithNotice(application));
    }

    private boolean isWithoutNoticeToWithNotice(GeneralApplication application) {
        return application.getJudicialDecisionRequestMoreInfo() != null
            && SEND_APP_TO_OTHER_PARTY.equals(application.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption())
            && !WITHOUT_NOTICE_STATES.contains(application.getGeneralApplicationState());
    }
}
