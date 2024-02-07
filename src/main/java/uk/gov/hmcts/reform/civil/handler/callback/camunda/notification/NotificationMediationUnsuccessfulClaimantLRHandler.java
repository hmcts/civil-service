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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NOT_ASSIGNED;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.PARTY_WITHDRAWS;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
@RequiredArgsConstructor
public class NotificationMediationUnsuccessfulClaimantLRHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR);
    private static final String LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR = "notification-mediation-unsuccessful-claimant-LR-%s";
    private static final String TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR = "SendMediationUnsuccessfulClaimantLR";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantLRForMediationUnsuccessful
    );
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    private CallbackResponse notifyClaimantLRForMediationUnsuccessful(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        sendEmail(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesForCARM(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private void sendEmail(final CaseData caseData) {
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        String emailTemplate = notificationsProperties.getMediationUnsuccessfulClaimantLRTemplate();

        if (featureToggleService.isCarmEnabledForCase(caseData.getSubmittedDate())) {
            sendMailAccordingToReason(caseData);
        } else {
            notificationService.sendMail(
                recipient,
                emailTemplate,
                addProperties(caseData),
                String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR, caseData.getLegacyCaseReference())
            );
        }

    }

    private void sendMailAccordingToReason(CaseData caseData) {
        if (findMediationUnsuccessfulReason(caseData,
                List.of(PARTY_WITHDRAWS, APPOINTMENT_NO_AGREEMENT, APPOINTMENT_NOT_ASSIGNED))) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getMediationUnsuccessfulLRTemplate(),
                addPropertiesForCARM(caseData),
                String.format(LOG_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR, caseData.getLegacyCaseReference()));
        }
    }
}
