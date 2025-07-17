package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ResponseDeadlineExtensionClaimantNotificationHandler
    extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION
    );

    public static final String TASK_ID = "DefendantResponseDeadlineExtensionNotifyClaimant";
    private static final String REFERENCE_TEMPLATE = "claimant-deadline-extension-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService toggleService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantForDeadlineExtension
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

    private CallbackResponse notifyClaimantForDeadlineExtension(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getEmail(caseData),
            getTemplate(caseData),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()) {
            HashMap<String, String> lipProperties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl(),
                RESPONSE_DEADLINE, formatLocalDate(
                    caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE
                )
            ));
            addAllFooterItems(caseData, lipProperties, configuration,
                              featureToggleService.isQueryManagementLRsEnabled(),
                              featureToggleService.isLipQueryManagementEnabled(caseData));
            return lipProperties;
        }
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData),
            AGREED_EXTENSION_DATE, formatLocalDate(
                caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));

        return properties;
    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private String getTemplate(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()) {
            return caseData.isClaimantBilingual() && featureToggleService.isDefendantNoCOnlineForCase(caseData)
                ? notificationsProperties.getClaimantLipDeadlineExtensionWelsh()
                : notificationsProperties.getClaimantLipDeadlineExtension();
        }
        return notificationsProperties.getClaimantDeadlineExtension();
    }

    private String getEmail(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && toggleService.isLipVLipEnabled()) {
            return caseData.getApplicant1Email();
        }
        return caseData.getApplicantSolicitor1UserDetails().getEmail();
    }
}
