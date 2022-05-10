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
import uk.gov.hmcts.reform.prd.model.OrganisationUser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CAA_RESPONDENT_1_ORG;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class NotifyCaseAccessAdmin extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CAA_RESPONDENT_1_ORG);

    private static final String TASK_ID = "TakeCaseOfflineNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "case-ready-for-assignment-%s";
    private static final String CASEWORKER_CAA = "caseworker-caa";

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
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyCAAUser(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        //get org ID for respondent 1
        var organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();

        //get users in this firm
        Optional<List<OrganisationUser>> orgUsers = organisationService.findUsersInOrganisation(
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            organisationId);

        List<String> caaEmails = orgUsers.get().stream()
            .filter(orgUser -> orgUser.getRoles().contains(CASEWORKER_CAA))
            .map(OrganisationUser::getEmail)
            .collect(Collectors.toList());

        System.out.print(caaEmails);

        String recipient;
        if (caaEmails.isEmpty()) {
            //no caa defined, use superuser for the firm
            System.out.print("NO CAA DEFINED");
            Optional<Organisation> organisation = organisationService.findOrganisationById(organisationId);
            recipient = organisation.get().getSuperUser().getEmail();
        } else {
            System.out.print("TAKING FIRST CAA FROM LIST");
            recipient = caaEmails.get(0);
        }

        System.out.print("About to send email to user: " + recipient);

        notificationService.sendMail(
            recipient,
            notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplateMultiParty(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getIssueDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
