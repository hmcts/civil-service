package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class ClaimDismissedApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED);
    public static final String TASK_ID = "ClaimDismissedNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE =
        "claim-dismissed-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    @Autowired
    private final StateFlowEngine stateFlowEngine;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyApplicantSolicitorForClaimDismissed
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForClaimDismissed(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String solicitorClaimDismissedProperty = getSolicitorClaimDismissedProperty(callbackParams.getCaseData());
        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
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
        return NotificationUtils.getSolicitorClaimDismissedProperty(
            stateFlowEngine.evaluate(caseData)
                .getStateHistory()
                .stream()
                .map(State::getName)
                .collect(Collectors.toList()),
            notificationsProperties
        );
    }
}
