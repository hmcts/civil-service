package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class MediationSuccessfulApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL);
    private static final String REFERENCE_TEMPLATE = "mediation-successful-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_LIP = "mediation-successful-applicant-notification-LIP-%s";
    public static final String TASK_ID = "MediationSuccessfulNotifyApplicant";
    private final Map<String, Callback> callbacksMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicant
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbacksMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyApplicant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isCarmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        if (isCarmEnabled) {
            String claimId = caseData.getLegacyCaseReference();
            String referenceTemplate = String.format(REFERENCE_TEMPLATE, claimId);
            MultiPartyScenario scenario = getMultiPartyScenario(caseData);
            if (Objects.nonNull(caseData.getApplicant1Represented())
                && caseData.isApplicant1NotRepresented()
                && featureToggleService.isLipVLipEnabled()) {
                sendEmail(
                    caseData.getApplicant1().getPartyEmail(),
                    caseData.isClaimantBilingual() ? notificationsProperties.getNotifyLipSuccessfulMediationWelsh() : notificationsProperties.getNotifyLipSuccessfulMediation(),
                    lipProperties(caseData),
                    referenceTemplate
                );
            } else if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP) || scenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                sendEmail(
                    caseData.getApplicantSolicitor1UserDetails().getEmail(),
                    notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation(),
                        oneVtwoProperties(caseData),
                    referenceTemplate
                );
            } else {
                // LR scenarios
                sendEmail(
                    caseData.getApplicantSolicitor1UserDetails().getEmail(),
                    notificationsProperties.getNotifyLrClaimantSuccessfulMediation(),
                    lrClaimantProperties(caseData),
                    referenceTemplate
                );
            }
        } else {
            if (Objects.nonNull(caseData.getApplicant1Represented())
                && caseData.isApplicant1NotRepresented()
                && featureToggleService.isLipVLipEnabled()) {
                notificationService.sendMail(
                    caseData.getApplicant1().getPartyEmail(),
                    addTemplate(caseData),
                    addPropertiesLip(caseData),
                    String.format(REFERENCE_TEMPLATE_LIP, caseData.getLegacyCaseReference()));
            } else {
                notificationService.sendMail(
                    caseData.getApplicantSolicitor1UserDetails().getEmail(),
                    notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate(),
                    addProperties(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    private String addTemplate(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getNotifyApplicantLiPMediationSuccessfulWelshTemplate() :
            notificationsProperties.getNotifyApplicantLiPMediationSuccessfulTemplate();
    }

    private void sendEmail(String targetEmail, String emailTemplate, Map<String, String> properties, String referenceTemplate) {
        notificationService.sendMail(
            targetEmail,
            emailTemplate,
            properties,
            referenceTemplate
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> lrClaimantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> oneVtwoProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> lipProperties(CaseData caseData) {
        return Map.of(
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {

        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

}
