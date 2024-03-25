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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediationSuccessfulApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL);
    private static final String REFERENCE_TEMPLATE = "mediation-successful-applicant-notification-%s";
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
        log.info("--Entered MediationSuccessfulApplicantNotificationHandler handler--");
        CaseData caseData = callbackParams.getCaseData();
        Boolean isCarmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        if (isCarmEnabled) {
            log.info("--Entered MediationSuccessfulApplicantNotificationHandler it is carm enabled--");
            String claimId = caseData.getCcdCaseReference().toString();
            String referenceTemplate = String.format(REFERENCE_TEMPLATE, claimId);
            MultiPartyScenario scenario = getMultiPartyScenario(caseData);
            if (caseData.isLipvLipOneVOne()) {
                log.info("--Entered MediationSuccessfulApplicantNotificationHandler lip v lip 1v1--");
                sendEmail(
                    caseData.getApplicant1().getPartyEmail(),
                    notificationsProperties.getNotifyLipSuccessfulMediation(),
                    lipProperties(caseData),
                    referenceTemplate
                );
            } else if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP) || scenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                log.info("--Entered MediationSuccessfulApplicantNotificationHandler 1v2--");
                sendEmail(
                    caseData.getApplicantSolicitor1UserDetails().getEmail(),
                    notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation(),
                    oneVtwoProperties(caseData),
                    referenceTemplate
                );
            } else {
                // LR scenarios
                log.info("--Entered MediationSuccessfulApplicantNotificationHandler 1v1/2v1--");
                sendEmail(
                    caseData.getApplicantSolicitor1UserDetails().getEmail(),
                    notificationsProperties.getNotifyLrClaimantSuccessfulMediation(),
                    lrClaimantProperties(caseData),
                    referenceTemplate
                );
            }
        } else {
            log.info("--Entered MediationSuccessfulApplicantNotificationHandler non carm--");
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();

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
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lrClaimantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> oneVtwoProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    public Map<String, String> lipProperties(CaseData caseData) {
        return Map.of(
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

}
