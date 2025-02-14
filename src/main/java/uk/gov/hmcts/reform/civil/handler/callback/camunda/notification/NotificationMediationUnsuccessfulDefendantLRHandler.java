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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;
import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_SAME_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationMediationUnsuccessfulDefendantLRHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR,
        NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR);
    private static final String LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR = "notification-mediation-unsuccessful-defendant-1-LR-%s";
    private static final String LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR = "notification-mediation-unsuccessful-defendant-2-LR-%s";
    private static final String TASK_ID_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR = "SendMediationUnsuccessfulDefendant1LR";
    private static final String TASK_ID_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR = "SendMediationUnsuccessfulDefendant2LR";
    private static final String DEFENDANTS_TEXT = "'s claim against you";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantLRForMediationUnsuccessful
    );
    private final FeatureToggleService featureToggleService;
    private final OrganisationDetailsService organisationDetailsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    private CallbackResponse notifyDefendantLRForMediationUnsuccessful(CallbackParams callbackParams) {
        if (featureToggleService.isCarmEnabledForCase(callbackParams.getCaseData())) {
            sendEmail(callbackParams);
        } else if (featureToggleService.isLipVLipEnabled()
            && callbackParams.getCaseData().isLipvLROneVOne()
            && isRespondentSolicitor1Notification(callbackParams)) {
            // Lip v LR scenario
            sendMailtoDefendantForLiPvLrCase(callbackParams);
        } else {
            log.info("Defendant LR is not notified because it is not a CARM case.");
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (isRespondentSolicitor1Notification(callbackParams)) {
            return TASK_ID_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR;
        }
        return TASK_ID_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR;
    }

    public Map<String, String> addProperties(final CaseData caseData) {
        String partyName = caseData.getApplicant1().getPartyName();

        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            partyName = String.format("%s and %s", partyName, caseData.getApplicant2().getPartyName());
        }

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            PARTY_NAME, partyName + DEFENDANTS_TEXT,
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> addPropertiesForDefendant2(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent2LegalOrganisationName(caseData),
            PARTY_NAME, caseData.getApplicant1().getPartyName() + DEFENDANTS_TEXT,
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> addPropertiesForDefendantLrForLipVLr(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> addPropertiesNoAttendanceCARM(CaseData caseData, boolean isDefendant1) {
        return Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, isDefendant1 ? organisationDetailsService.getRespondent1LegalOrganisationName(caseData)
                          : organisationDetailsService.getRespondent2LegalOrganisationName(caseData),
                      CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }

    private void sendEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (isRespondent1LRAndNotContactable(callbackParams)) {
            sendMailDefendant1NoAttendance(caseData);
        } else if (isRespondent2LRAndNotContactable(callbackParams)) {
            sendMailDefendant2NoAttendance(caseData);
        } else {
            sendGenericMailtoDefendant(callbackParams);
        }
    }

    private boolean isRespondent1LRAndNotContactable(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isRespondentSolicitor1Notification(callbackParams)
            && (findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE))
            || isSameSolicitorAndNonContactableDefendant2(caseData));
    }

    private boolean isRespondent2LRAndNotContactable(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return isRespondentSolicitor2Notification(callbackParams)
            && findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_TWO));
    }

    private boolean isSameSolicitorAndNonContactableDefendant2(CaseData caseData) {
        // 1v2SS and mediation reason = NOT_CONTACTABLE_DEFENDANT_TWO -> defendant 1 LR should be notified
        return ONE_V_TWO_SAME_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_TWO));
    }

    private void sendGenericMailtoDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isRespondentSolicitor1Notification(callbackParams)) {
            sendGenericMailToDefendant1Solicitor(caseData);
        } else {
            sendGenericMailToDefendant2Solicitor(caseData);
        }
    }

    private void sendMailtoDefendantForLiPvLrCase(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isRespondentSolicitor1Notification(callbackParams)) {
            sendGenericMailToDefendant1SolicitorForLipVLrCase(caseData);
        }
    }

    private void sendGenericMailToDefendant1Solicitor(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulLRTemplate(),
            addProperties(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR, caseData.getLegacyCaseReference())
        );
    }

    private void sendGenericMailToDefendant2Solicitor(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor2EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulLRTemplate(),
            addPropertiesForDefendant2(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR, caseData.getLegacyCaseReference())
        );
    }

    private void sendMailDefendant1NoAttendance(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate(),
            addPropertiesNoAttendanceCARM(caseData, true),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR, caseData.getLegacyCaseReference()));
    }

    private void sendMailDefendant2NoAttendance(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor2EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate(),
            addPropertiesNoAttendanceCARM(caseData, false),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR, caseData.getLegacyCaseReference()));
    }

    private boolean isRespondentSolicitor1Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name());
    }

    private boolean isRespondentSolicitor2Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR.name());
    }

    private void sendGenericMailToDefendant1SolicitorForLipVLrCase(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulLRTemplateForLipVLr(),
            addPropertiesForDefendantLrForLipVLr(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR, caseData.getLegacyCaseReference())
        );
    }
}
