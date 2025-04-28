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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class CaseProceedsInCasemanApplicantNotificationCallbackHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN);
    private final FeatureToggleService featureToggleService;
    protected static final List<String> TASK_IDS =
        Arrays.asList("CaseProceedsInCasemanNotifyApplicantSolicitor1",
                      "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForUnRegisteredFirm",
                      "NotifyClaimProceedsOfflineNotifyApplicantSolicitor1",
                      "NotifyClaimDetailsProceedOfflineApplicantSolicitor1CC");
    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForCaseProceedsInCaseman
        );
    }

    @Override
    public List<String> camundaActivityIds(CallbackParams callbackParams) {
        return TASK_IDS;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForCaseProceedsInCaseman(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        notificationService.sendMail(
            caseData.isLipvLROneVOne() ? caseData.getApplicant1Email() :
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getEmailTemplate(caseData),
            caseData.isLipvLROneVOne() ? addPropertiesForLip(caseData) : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getEmailTemplate(CaseData caseData) {
        return notificationsProperties.getTestTemplate();
    }

    private Map<String, String> addPropertiesForLip(CaseData caseData) {

        Map<String, String> properties = new HashMap<>();

        properties.putAll(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, "abc",
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_DETAILS_NOTIFICATION_DEADLINE,
            formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)));

        if (caseData.getRespondent2() != null) {
            properties.put(RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
        } else {
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));
        }

        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(properties, configuration);
        return properties;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        Map<String, String> properties = new HashMap<>();

        properties.putAll(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, "abc",
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_DETAILS_NOTIFICATION_DEADLINE,
            formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)));

        if (caseData.getRespondent2() != null) {
            properties.put(RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
        } else {
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(RESPONSE_DEADLINE, formatLocalDateTime(
                caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));
        }

        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(properties, configuration);
        return properties;
    }
}
