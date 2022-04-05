package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class AgreedExtensionDateApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static Map<CaseEvent, String> EVENT_TASK_ID_MAP = Map.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE, "AgreedExtensionDateNotifyApplicantSolicitor1",
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC, "AgreedExtensionDateNotifyRespondentSolicitor1CC",
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC, "AgreedExtensionDateNotifyRespondentSolicitor2CC"
    );

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
        return EVENT_TASK_ID_MAP.get(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return new ArrayList<>(EVENT_TASK_ID_MAP.keySet());
    }

    private CallbackResponse notifyApplicantSolicitorForAgreedExtensionDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getSolicitorEmailAddress(callbackParams),
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
                && (caseData.getRespondent2TimeExtensionDate() == null)) {
                extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
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
            AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

    private String getSolicitorEmailAddress(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        CaseData caseData = callbackParams.getCaseData();

        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE.name())) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        }
        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC.name())) {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
        if (eventId.equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC.name())) {
            return caseData.getRespondentSolicitor2EmailAddress();
        }

        throw new CallbackException(String.format("Callback handler received unexpected event id: %s", eventId));
    }
}
