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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class AgreedExtensionDateApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC);

    public static final String TASK_ID = "AgreedExtensionDateNotifyApplicantSolicitor1";
    public static final String TASK_ID_CC = "AgreedExtensionDateNotifyRespondentSolicitor1CC";
    private static final String REFERENCE_TEMPLATE = "agreed-extension-date-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForAgreedExtensionDate
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isCcNotification(callbackParams) ? TASK_ID_CC : TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForAgreedExtensionDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var recipient = isCcNotification(callbackParams)
            ? getRespondentSolicitorEmailAddress(caseData)
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorAgreedExtensionDate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        LocalDate extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();

        //finding extension date for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1TimeExtensionDate() == null)
                && (caseData.getRespondent2TimeExtensionDate() != null)) {
                extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
            } else if ((caseData.getRespondent1TimeExtensionDate() != null)
                && (caseData.getRespondent2TimeExtensionDate() != null)) {
                if (caseData.getRespondent2TimeExtensionDate()
                    .isAfter(caseData.getRespondent1TimeExtensionDate())) {
                    extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
                } else {
                    extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
                }
            }
        }

        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE)
        );
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC.name());
    }

    private String getRespondentSolicitorEmailAddress(CaseData caseData) {
        String respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();

        //finding email for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1TimeExtensionDate() == null)
                && (caseData.getRespondent2TimeExtensionDate() != null)) {
                respondentSolicitorEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
            } else if ((caseData.getRespondent1TimeExtensionDate() != null)
                && (caseData.getRespondent2TimeExtensionDate() != null)) {
                if (caseData.getRespondent2TimeExtensionDate()
                    .isAfter(caseData.getRespondent1TimeExtensionDate())) {
                    respondentSolicitorEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
                } else {
                    respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
                }
            }
        }
        return respondentSolicitorEmailAddress;
    }
}
