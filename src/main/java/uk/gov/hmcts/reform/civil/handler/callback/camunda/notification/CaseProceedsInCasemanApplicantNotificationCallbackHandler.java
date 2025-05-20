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
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addLipContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

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
        if (caseData.isLipvLROneVOne()) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate();
            }
            return notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        }

        return notificationsProperties.getSolicitorCaseTakenOffline();
    }

    private Map<String, String> addPropertiesForLip(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        ));
        addCommonFooterSignature(properties, configuration);
        addLipContact(caseData, properties, featureToggleService.isQueryManagementLRsEnabled(),
                      featureToggleService.isLipQueryManagementEnabled(caseData));
        return properties;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }
}
