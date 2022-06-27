package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.OrganisationUtils;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class NotifyCAAHandler extends CallbackHandler implements NotificationData {

    private static Map<CaseEvent, String> EVENT_TASK_ID_MAP = Map.of(
        NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG, "NotifyClaimCAARespondent1Org",
        NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG, "NotifyClaimCAARespondent2Org"
    );

    private static final String REFERENCE_TEMPLATE = "create-claim-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyCAAUser
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return EVENT_TASK_ID_MAP.get(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return new ArrayList<>(EVENT_TASK_ID_MAP.keySet());
    }

    private CallbackResponse notifyCAAUser(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        List<String> recipients = new ArrayList<>();

        switch(caseEvent) {
            case NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG:
                recipients.addAll(getRecipients(
                    getOrganisationId(caseData, !shouldNotifyOnlyRespondent2Caa(caseData))));
                break;
            case NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG:
                recipients.addAll(getRecipients(getOrganisationId(caseData, false)));
                break;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }


        recipients.forEach(recipient -> {
            System.out.println("About to send email to user: " + recipient);
            notificationService.sendMail(
                recipient,
                notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        });

        //return AboutToStartOrSubmitCallbackResponse.builder().build();
        throw new CallbackException("COMPLETED!!");
    }

    private List<String> getRecipients(String organisationId) {
        var caaEmails = OrganisationUtils.getCaaEmails(organisationService.findUsersInOrganisation(organisationId));
        if (caaEmails.isEmpty()) {
            Optional<Organisation> organisation = organisationService.findOrganisationById(organisationId);
            caaEmails.add(organisation.get().getSuperUser().getEmail());
        }
        return caaEmails;
    }

    private boolean shouldNotifyOnlyRespondent2Caa(CaseData caseData) {
        return caseData.getDefendantSolicitorNotifyClaimOptions() != null
            && caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel()
            .startsWith("Defendant Two:");
    }

    private String getOrganisationId(CaseData caseData, boolean isRespondent1) {
        return isRespondent1 ?
            caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_NOTIFICATION_DEADLINE,
            formatLocalDate(caseData.getClaimNotificationDeadline().toLocalDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
