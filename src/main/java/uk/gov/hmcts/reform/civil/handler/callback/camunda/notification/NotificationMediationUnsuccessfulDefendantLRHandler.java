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
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NOT_ASSIGNED;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.PARTY_WITHDRAWS;
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
        if (featureToggleService.isCarmEnabledForCase(callbackParams.getCaseData().getSubmittedDate())) {
            sendEmail(callbackParams);
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
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesForDefendant2(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent2LegalOrganisationName(caseData),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private void sendEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (findMediationUnsuccessfulReason(caseData,
                List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT, APPOINTMENT_NOT_ASSIGNED))) {
            if (isRespondentSolicitor1Notification(callbackParams)) {
                sendMailToDefendant1Solicitor(caseData);
            } else {
                sendMailToDefendant2Solicitor(caseData);
            }
        }
    }

    private void sendMailToDefendant1Solicitor(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulLRTemplate(),
            addProperties(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR, caseData.getLegacyCaseReference())
        );
    }

    private void sendMailToDefendant2Solicitor(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondentSolicitor2EmailAddress(),
            notificationsProperties.getMediationUnsuccessfulLRTemplate(),
            addPropertiesForDefendant2(caseData),
            String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR, caseData.getLegacyCaseReference())
        );
    }

    private boolean isRespondentSolicitor1Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name());
    }
}
