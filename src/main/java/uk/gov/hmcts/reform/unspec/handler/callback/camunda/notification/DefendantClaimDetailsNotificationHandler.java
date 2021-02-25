package uk.gov.hmcts.reform.unspec.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DefendantClaimDetailsNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS);
    public static final String TASK_ID = "NotifyClaimDetailsRespondentSolicitor1";
    private static final String REFERENCE_TEMPLATE = "claim-details-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimDetails
        );
    }

    @Override
    public String camundaActivityId() {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        CaseData updatedCaseData = caseData.toBuilder()
            .claimDetailsNotificationDate(LocalDate.now())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(updatedCaseData))
            .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            ISSUED_ON, formatLocalDate(caseData.getClaimIssuedDate(), DATE)
        );
    }
}
