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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.*;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimContinuingOnlineApplicantForSpecNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    public static final String TASK_ID = "CreateClaimContinuingOnlineNotifyApplicantSolicitor1ForSpec";
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForClaimContinuingOnline
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

    private CallbackResponse notifyApplicantSolicitorForClaimContinuingOnline(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            RESPONSE_DEADLINE, formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT)
        );
    }
}
