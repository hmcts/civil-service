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
public class MediationSuccessfulRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL);
    private static final String REFERENCE_TEMPLATE = "mediation-successful-respondent-notification-%s";
    public static final String TASK_ID = "MediationSuccessfulNotifyRespondent";
    private final Map<String, Callback> callbacksMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondent
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbacksMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyRespondent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Boolean isCarmEnabled = featureToggleService.isCarmEnabledForCase(caseData.getSubmittedDate());
        if (isCarmEnabled) {
            String claimId = caseData.getCcdCaseReference().toString();
            String referenceTemplate = String.format(REFERENCE_TEMPLATE, claimId);
            String defendantEmail =  caseData.getRespondentSolicitor1EmailAddress();
            MultiPartyScenario scenario = getMultiPartyScenario(caseData);
            //LR v LR
            if (scenario.equals(ONE_V_ONE) && !(caseData.isLipvLipOneVOne()  || caseData.isLRvLipOneVOne())) {
                //send notification to the defendant
                sendEmail(
                    defendantEmail,
                    notificationsProperties.getNotifyLrVLrDefendantSuccessfulMediation(),
                    lrVLrDefendantProperties(caseData),
                    referenceTemplate);
            } else
                // LR v LR -> 1V2 -> same defendant
                if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
                    //send notification to the defendant
                    sendEmail(
                        defendantEmail,
                        notificationsProperties.getNotifyLrVLrOneVTwoSameSolicitorDefendantSuccessfulMediation(),
                        lrVLrSameSolicitorProperties(caseData),
                        referenceTemplate);
                    return AboutToStartOrSubmitCallbackResponse.builder().build();
                } else
                    // LR v LR -> 1V2 -> different defendant
                    if (scenario.equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                        sendEmail(
                            defendantEmail,
                            notificationsProperties.getNotifyLrVLrOneVTwoDifferentSolicitorsDefendantSuccessfulMediation(),
                            lrVLrDifferentSolicitorRespondent1Properties(caseData),
                            referenceTemplate);
                        sendEmail(
                            caseData.getRespondentSolicitor2EmailAddress(),
                            notificationsProperties.getNotifyLrVLrOneVTwoDifferentSolicitorsDefendantSuccessfulMediation(),
                            lrVLrDifferentSolicitorRespondent2Properties(caseData),
                            referenceTemplate);
                    } else
                        // LR v LR -> 2V1
                        if (scenario.equals(TWO_V_ONE)) {
                            sendEmail(
                                defendantEmail,
                                notificationsProperties.getNotifyLrVLrTwoVOneDefendantSuccessfulMediation(),
                                twoVOneDefendantProperties(caseData),
                                referenceTemplate);
                        } else
                            // LR v LIP
                            if (caseData.isLRvLipOneVOne()) {
                                sendEmail(
                                    defendantEmail,
                                    notificationsProperties.getNotifyLrVLipDefendantSuccessfulMediation(),
                                    lrVLipDefendantProperties(caseData),
                                    referenceTemplate);
                            } else
                                // LIP v LIP
                                if (caseData.isLipvLipOneVOne()) {
                                    sendEmail(
                                        caseData.getRespondent1().getPartyEmail(),
                                        notificationsProperties.getNotifyLipVLipDefendantSuccessfulMediation(),
                                        lipVLipDefendantProperties(caseData),
                                        referenceTemplate);
                                }
        } else {
            if (caseData.isRespondent1NotRepresented() && caseData.getRespondent1().getPartyEmail() != null) {
                notificationService.sendMail(
                    caseData.getRespondent1().getPartyEmail(),
                    addTemplate(caseData),
                    addProperties(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
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
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    private String addTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplateWelsh() :
            notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate();
    }

    public Map<String, String> lrVLrDefendantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> lrVLrSameSolicitorProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> lrVLrDifferentSolicitorRespondent1Properties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> lrVLrDifferentSolicitorRespondent2Properties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent2LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> twoVOneDefendantProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName(),
            CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName()
        );
    }

    public Map<String, String> lrVLipDefendantProperties(CaseData caseData) {
        return Map.of(
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> lipVLipDefendantProperties(CaseData caseData) {
        return Map.of(
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

}
