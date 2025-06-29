package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_PART_ADMIT_PAY_IMMEDIATELY_AGREED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_PART_ADMIT_PAY_IMMEDIATELY_AGREED;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Service
@RequiredArgsConstructor
public class PartAdmitPayImmediatelyNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT1_PART_ADMIT_PAY_IMMEDIATELY_AGREED,
        NOTIFY_APPLICANT_PART_ADMIT_PAY_IMMEDIATELY_AGREED
    );

    public static final String TASK_ID_RESPONDENT = "DefendantPartAdmitPayImmediatelyNotification";
    public static final String TASK_ID_APPLICANT = "ClaimantPartAdmitPayImmediatelyNotification";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "part-admit-immediately-agreed-respondent-notification-%s";
    private static final String REFERENCE_TEMPLATE_APPLICANT = "part-admit-immediately-agreed-applicant-notification-%s";

    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    
    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyPartAdmitPayImmediately
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent(callbackParams)
            ? TASK_ID_RESPONDENT : TASK_ID_APPLICANT;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyPartAdmitPayImmediately(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String emailAddress = isRespondent(callbackParams)
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

        if (emailAddress != null) {
            Map<String, String> notificationProperties = addProperties(caseData);
            String legalOrganisation = isRespondent(callbackParams)
                ? getLegalOrganizationNameForRespondent(caseData, true, organisationService)
                : getApplicantLegalOrganizationName(caseData, organisationService);
            notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrganisation);

            notificationService.sendMail(
                emailAddress,
                isRespondent(callbackParams)
                    ? notificationsProperties.getPartAdmitPayImmediatelyAgreedDefendant()
                    : notificationsProperties.getPartAdmitPayImmediatelyAgreedClaimant(),
                notificationProperties,
                String.format(getReferenceTemplate(callbackParams), caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public static String getReferenceTemplate(CallbackParams callbackParams) {
        return isRespondent(callbackParams)
            ? REFERENCE_TEMPLATE_RESPONDENT : REFERENCE_TEMPLATE_APPLICANT;
    }

    private static boolean isRespondent(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT1_PART_ADMIT_PAY_IMMEDIATELY_AGREED.name());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
        return properties;
    }
}
