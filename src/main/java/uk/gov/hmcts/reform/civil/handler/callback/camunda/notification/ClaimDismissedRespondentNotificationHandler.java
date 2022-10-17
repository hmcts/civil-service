package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.*;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

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
    private final StateFlowEngine stateFlowEngine;

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
        String recipient = !is1v1Or2v1Case(caseData)
            && !isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED) ? caseData
                .getRespondentSolicitor2EmailAddress() : caseData.getRespondentSolicitor1EmailAddress();

        String solicitorClaimDismissedProperty = getSolicitorClaimDismissedProperty(callbackParams.getCaseData());

        notificationService.sendMail(
            recipient,
            solicitorClaimDismissedProperty,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

    private String getSolicitorClaimDismissedProperty(CaseData caseData) {

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        List<String> stateHistoryNameList = stateFlow.getStateHistory().stream().map(State::getName).collect(Collectors.toList());

        //scenerio 1: Claim notification does not happen within 4 months of issue
        if (stateHistoryNameList.contains(CLAIM_ISSUED.fullName())
            && !stateHistoryNameList.contains(CLAIM_NOTIFIED.fullName())
            && ifPastClaimNotificationDeadline(caseData)) {
            return notificationsProperties.getSolicitorClaimDismissedWithin4Months();
        }//scenerio 2: Claims details notification is not completed within 14 days of the claim notification step
        else if (stateHistoryNameList.contains(CLAIM_NOTIFIED.fullName())
            && stateHistoryNameList.contains(AWAITING_CASE_DETAILS_NOTIFICATION.name())
            && !stateHistoryNameList.contains(CLAIM_DETAILS_NOTIFIED.fullName())
            && ifPastClaimDetailsNotificationDeadline(caseData)) {
            return notificationsProperties.getSolicitorClaimDismissedWithin4Months();
        }//scenerio 3 Claimant does not give their intention by the given deadline
        else if(stateHistoryNameList.contains(CLAIM_DETAILS_NOTIFIED.fullName())
            && stateHistoryNameList.contains(AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name())
            && ifPastClaimDismissedDeadline(caseData)){
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        }
        else{
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        }
    }

    private boolean ifPastClaimDismissedDeadline(CaseData caseData) {
        return caseData.getClaimDismissedDate() != null
            && caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now());
    }

    private boolean ifPastClaimNotificationDeadline(CaseData caseData) {
        return caseData.getClaimDismissedDate() != null
            && caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimNotificationDate() == null;
    }

    private boolean ifPastClaimDetailsNotificationDeadline(CaseData caseData) {
        return caseData.getClaimDismissedDate() != null
            && caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getClaimNotificationDate() != null;
    }
}
