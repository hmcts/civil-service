package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSkipEventForRespondent1LiP;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSkipEventForRespondent2LiP;

@Service
@RequiredArgsConstructor
public class ClaimDismissedRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED
    );
    public static final String TASK_ID_RESPONDENT1 = "ClaimDismissedNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "ClaimDismissedNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE =
        "claim-dismissed-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final IStateFlowEngine stateFlowEngine;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyRespondentSolicitorForClaimDismissed
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED) ? TASK_ID_RESPONDENT1
            : TASK_ID_RESPONDENT2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDismissed(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent event = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        if ((NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED.equals(event)
            && shouldSkipEventForRespondent1LiP(caseData))
            || (NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED.equals(event)
            && shouldSkipEventForRespondent2LiP(caseData))) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        String recipient = !is1v1Or2v1Case(caseData)
            && !isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED) ? caseData
            .getRespondentSolicitor2EmailAddress() : caseData.getRespondentSolicitor1EmailAddress();

        String solicitorClaimDismissedProperty = getSolicitorClaimDismissedProperty(callbackParams.getCaseData());

        Map<String, String> notificationProperties = addProperties(caseData);
        notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentOrgName(callbackParams, caseData));

        notificationService.sendMail(
            recipient,
            solicitorClaimDismissedProperty,
            notificationProperties,
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private String getRespondentOrgName(CallbackParams callbackParams, CaseData caseData) {
        return !is1v1Or2v1Case(caseData)
            && !isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED)
            ? getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService)
            : getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService);
    }

    private String getSolicitorClaimDismissedProperty(CaseData caseData) {
        return NotificationUtils.getSolicitorClaimDismissedProperty(
            stateFlowEngine.evaluate(caseData).getStateHistory()
                .stream()
                .map(State::getName)
                .toList(),
            notificationsProperties
        );
    }
}
