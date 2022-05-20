package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildClaimantReference;

@Service
@RequiredArgsConstructor
public class ClaimContinuingOnlineApplicantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE);
    public static final String TASK_ID = "CreateClaimContinuingOnlineNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";
    private static final String CASEWORKER_CAA_ROLE = "pui-caa";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForClaimContinuingOnline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForClaimContinuingOnline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        //get org ID for respondent 1
        var organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();

        //get users in this firm
        Optional<ProfessionalUsersEntityResponse> orgUsers =
            organisationService.findUsersInOrganisation(organisationId);

        //identify caa users based on user roles
        List<String> caaEmails = new ArrayList<>();

        assert orgUsers.orElse(null) != null;
        for (ProfessionalUsersResponse user : orgUsers.get().getUsers()) {
            if (user.getRoles().contains(CASEWORKER_CAA_ROLE)) {
                String email = user.getEmail();
                caaEmails.add(email);
            }
        }
        System.out.print(caaEmails);

        String recipient;
        if (! caaEmails.isEmpty()) {
            System.out.println("TAKING FIRST CAA FROM LIST");
            recipient = caaEmails.get(0);
        } else {
            //no CAA defined, use superuser for the firm
            System.out.println("NO CAA DEFINED");
            Optional<Organisation> organisation = organisationService.findOrganisationById(organisationId);

            if (organisation.isPresent()) {
                recipient = organisation.get().getSuperUser().getEmail();
            } else {

                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of("Firm does not have CAA or Admin configured"))
                    .build();
            }

        }

        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorClaimContinuingOnline(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            NOTIFICATION_DEADLINE, formatLocalDate(caseData.getClaimNotificationDeadline().toLocalDate(), DATE),
            PARTY_REFERENCES, buildClaimantReference(caseData)
        );
    }
}
