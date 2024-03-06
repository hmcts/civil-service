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

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
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
        Boolean isCarmEnabled = featureToggleService.isCarmEnabledForCase(caseData.getSubmittedDate());
        if (isCarmEnabled) {
            String claimId = caseData.getCcdCaseReference().toString();
            String referenceTemplate = String.format(REFERENCE_TEMPLATE, claimId);
            String application1Email = caseData.getApplicantSolicitor1UserDetails().getEmail();
            MultiPartyScenario scenario = getMultiPartyScenario(caseData);
            //LR v LR
            if (scenario.equals(ONE_V_ONE) && !(caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne())) {
                //send notification to the claimant
                sendEmail(
                    application1Email,
                    notificationsProperties.getNotifyLrVLrClaimantSuccessfulMediation(),
                    lrVLrClaimantProperties(caseData),
                    referenceTemplate);
            } else
                // LR v LR -> 1V2 -> same solicitor
                if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
                    //send notification to the claimant
                    sendEmail(
                        application1Email,
                        notificationsProperties.getNotifyLrVLrOneVTwoSameSolicitorClaimantSuccessfulMediation(),
                        lrVLrSameSolicitorProperties(caseData),
                        referenceTemplate);
                    return AboutToStartOrSubmitCallbackResponse.builder().build();
                } else
                    // LR v LR -> 1V2 -> different solicitor
                    if (scenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                        sendEmail(
                            application1Email,
                            notificationsProperties.getNotifyLrVLrOneVTwoDifferentSolicitorsClaimantSuccessfulMediation(),
                            lrVLrDifferentSolicitorProperties(caseData),
                            referenceTemplate);
                    } else
                        // LR v LR -> 2V1
                        if (scenario.equals(TWO_V_ONE)) {
                            sendEmail(
                                application1Email,
                                notificationsProperties.getNotifyLrVLrTwoVOneClaimantSuccessfulMediation(),
                                twoVOneClaimantProperties(caseData),
                                referenceTemplate);
                        } else
                            // LR v LIP
                            if (caseData.isLRvLipOneVOne()) {
                                sendEmail(
                                    application1Email,
                                    notificationsProperties.getNotifyLrVLipClaimantSuccessfulMediation(),
                                    lrVLipClaimantProperties(caseData),
                                    referenceTemplate);
                            } else
                                // LIP v LIP
                                if (caseData.isLipvLipOneVOne()) {
                                    sendEmail(
                                        application1Email,
                                        notificationsProperties.getNotifyLipVLipClaimantSuccessfulMediation(),
                                        lipVLipClaimantProperties(caseData),
                                        referenceTemplate);
                                }
        } else {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    private void sendEmail(String targetEmail, String emailTemplate, Map<String, String> properties, String referenceTemplate) {
        notificationService.sendMail(
            targetEmail,
            emailTemplate,
            properties,
            String.format(REFERENCE_TEMPLATE, referenceTemplate)
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

    public Map<String, String> lrVLrClaimantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lrVLrSameSolicitorProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    public Map<String, String> lrVLrDifferentSolicitorProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    public Map<String, String> twoVOneClaimantProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lrVLipClaimantProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lipVLipClaimantProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

}
