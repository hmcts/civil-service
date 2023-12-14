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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimContinuingOnlineApplicantPartyForSpecNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    public static final String TASK_ID_Applicant1 = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantForClaimContinuingOnline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_Applicant1;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantForClaimContinuingOnline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        generateEmail(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline()
                                                   .toLocalDate(), DATE)
        );
    }

    private void generateEmail(CaseData caseData) {
        notificationService.sendMail(
            caseData.getApplicant1Email(),
            notificationsProperties.getClaimantClaimContinuingOnlineForSpec(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }
}
