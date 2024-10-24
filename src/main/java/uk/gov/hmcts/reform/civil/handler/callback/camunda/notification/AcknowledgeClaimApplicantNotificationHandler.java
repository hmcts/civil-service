package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcknowledgeClaimApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC);

    public static final String TASK_ID = "AcknowledgeClaimNotifyApplicantSolicitor1";
    public static final String TASK_ID_CC = "AcknowledgeClaimNotifyRespondentSolicitor1CC";
    private static final String REFERENCE_TEMPLATE = "acknowledge-claim-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForClaimAcknowledgement
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

    private CallbackResponse notifyApplicantSolicitorForClaimAcknowledgement(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var recipient = isCcNotification(callbackParams)
            ? getRespondentSolicitorEmailAddress(caseData)
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

        Map<String, String> notificationProperties = addProperties(caseData);
        notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getOrgName(caseData, callbackParams));

        if (recipient != null) {
            notificationService.sendMail(
                recipient,
                notificationsProperties.getRespondentSolicitorAcknowledgeClaim(),
                notificationProperties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else {
            log.info(String.format("Email address is null for %s", caseData.getLegacyCaseReference()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
        Party respondent = caseData.getRespondent1();
        //finding response deadline date for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                responseDeadline = caseData.getRespondent2ResponseDeadline();
                respondent = caseData.getRespondent2();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() == null)) {
                responseDeadline = caseData.getRespondent1ResponseDeadline();
                respondent = caseData.getRespondent1();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                    .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    responseDeadline = caseData.getRespondent2ResponseDeadline();
                    respondent = caseData.getRespondent2();
                } else {
                    responseDeadline = caseData.getRespondent1ResponseDeadline();
                    respondent = caseData.getRespondent1();
                }
            }
        }

        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONDENT_NAME, getPartyNameBasedOnType(respondent),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE),
            RESPONSE_INTENTION, getResponseIntentionForEmail(caseData)
        ));
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC.name());
    }

    private String getRespondentSolicitorEmailAddress(CaseData caseData) {
        String respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();

        //finding email for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                respondentSolicitorEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() == null)) {
                respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                    .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    respondentSolicitorEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
                } else {
                    respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
                }
            }
        }
        return respondentSolicitorEmailAddress;
    }

    private String getOrgName(CaseData caseData, CallbackParams callbackParams) {
        return isCcNotification(callbackParams)
            ? getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
            : getApplicantLegalOrganizationName(caseData, organisationService);
    }
}
