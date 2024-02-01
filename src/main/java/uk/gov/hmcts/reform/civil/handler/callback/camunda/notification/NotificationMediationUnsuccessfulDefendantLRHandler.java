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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR;
@Service
@RequiredArgsConstructor
public class NotificationMediationUnsuccessfulDefendantLRHandler extends CallbackHandler implements NotificationData {

    private final OrganisationDetailsService organisationDetailsService;
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
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    private CallbackResponse notifyDefendantLRForMediationUnsuccessful(CallbackParams callbackParams) {
        sendEmail(callbackParams);
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
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, false),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesForDefendant2(final CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, true),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private void sendEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();

        //TODO: this template has to change nameNotificationMediationUnsuccessfulDefendant1LRHandler
        String emailTemplate = notificationsProperties.getMediationUnsuccessfulClaimantLRTemplate();
        System.out.println(recipient);
        System.out.println(emailTemplate);

        if (isRespondentSolicitor1Notification(callbackParams)) {
            notificationService.sendMail(
                "sherlyn.khaw1@hmcts.net",
                emailTemplate,
                addProperties(caseData),
                String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR, caseData.getLegacyCaseReference())
            );
        } else {
            notificationService.sendMail(
                "sherlyn.khaw1@hmcts.net",
                emailTemplate,
                addPropertiesForDefendant2(caseData),
                String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR, caseData.getLegacyCaseReference())
            );
        }

    }

    private boolean isRespondentSolicitor1Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR.name());
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData, boolean isDefendant1) {
        String organisationID = isDefendant1
            ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
