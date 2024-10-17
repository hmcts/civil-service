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
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

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
    private final OrganisationService organisationService;

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
        Map<String, String> notificationProperties = addProperties(caseData);
        notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getSolicitorOrgName(callbackParams));

        notificationService.sendMail(
            getSolicitorEmailAddress(callbackParams),
            notificationsProperties.getClaimantSolicitorAgreedExtensionDate(),
            notificationProperties,
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        LocalDateTime extensionDate = caseData.getRespondent1ResponseDeadline();

        //finding extension date for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if ((multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) || (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP)) {
            if ((caseData.getRespondent1ResponseDeadline() == null)
                && (caseData.getRespondent2ResponseDeadline() != null)) {
                extensionDate = caseData.getRespondent2ResponseDeadline();
            } else if ((caseData.getRespondent1ResponseDeadline() != null)
                && (caseData.getRespondent2ResponseDeadline() == null)) {
                extensionDate = caseData.getRespondent1ResponseDeadline();
            } else if ((caseData.getRespondent1ResponseDeadline() != null)
                && (caseData.getRespondent2ResponseDeadline() != null)) {
                if (caseData.getRespondent2ResponseDeadline()
                    .isAfter(caseData.getRespondent1ResponseDeadline())) {
                    extensionDate = caseData.getRespondent2ResponseDeadline();
                } else {
                    extensionDate = caseData.getRespondent1ResponseDeadline();
                }
            }
        }

        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            AGREED_EXTENSION_DATE, formatLocalDate(extensionDate.toLocalDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            DEFENDANT_NAME, fetchDefendantName(caseData)
        ));
    }

    private String getSolicitorOrgName(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        CaseData caseData = callbackParams.getCaseData();

        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE.name())) {
            return getApplicantLegalOrganizationName(caseData, organisationService);
        }
        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC.name())) {
            return getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService);
        }
        if (eventId.equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC.name())) {
            return getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService);
        }

        throw new CallbackException(String.format("Callback handler received unexpected event id: %s", eventId));
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
