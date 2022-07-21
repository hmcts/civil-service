package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.OrganisationUtils;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_1_ORG;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_2_ORG;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class DefendantClaimDetailsNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC,
        NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_1_ORG,
        NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_2_ORG
    );

    public static final String TASK_ID_EMAIL_FIRST_SOL = "NotifyClaimDetailsRespondentSolicitor1";
    public static final String TASK_ID_EMAIL_APP_SOL_CC = "NotifyClaimDetailsApplicantSolicitor1CC";
    public static final String TASK_ID_EMAIL_SECOND_SOL = "NotifyClaimDetailsRespondentSolicitor2";
    public static final String TASK_ID_EMAIL_FIRST_CAA = "NotifyClaimDetailsRespondent1OrgCAA";
    public static final String TASK_ID_EMAIL_SECOND_CAA = "NotifyClaimDetailsRespondent2OrgCAA";

    private static final String REFERENCE_TEMPLATE = "claim-details-respondent-notification-%s";
    private static final int EMAIL_LIMIT = 100;

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimDetails
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return TASK_ID_EMAIL_FIRST_SOL;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return TASK_ID_EMAIL_APP_SOL_CC;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return TASK_ID_EMAIL_SECOND_SOL;
            case NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_1_ORG:
                return TASK_ID_EMAIL_FIRST_CAA;
            case NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_2_ORG:
                return TASK_ID_EMAIL_SECOND_CAA;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        String emailTemplate = notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplate();

        notificationService.sendNotifications(
            getRecipientEmails(caseData, caseEvent),
            emailTemplate, addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private List<String> getRecipientEmails(CaseData caseData, CaseEvent caseEvent) {
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return Arrays.asList(shouldNotifyOnlyRespondent2Party(caseData)
                    ? caseData.getRespondentSolicitor2EmailAddress() : caseData.getRespondentSolicitor1EmailAddress());
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return Arrays.asList(caseData.getApplicantSolicitor1UserDetails().getEmail());
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return Arrays.asList(caseData.getRespondentSolicitor2EmailAddress());
            case NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_1_ORG:
                return getCaaEmails(getRespondentOrganisationId(caseData, !shouldNotifyOnlyRespondent2Party(caseData)));
            case NOTIFY_CLAIM_DETAILS_CAA_RESPONDENT_2_ORG:
                return getCaaEmails(getRespondentOrganisationId(caseData, false));
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    private boolean shouldNotifyOnlyRespondent2Party(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimDetailsOptions())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("")
            .startsWith("Defendant Two:");
    }

    private String getRespondentOrganisationId(CaseData caseData, boolean isRespondent1) {
        return isRespondent1 ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
    }

    private List<String> getCaaEmails(String organisationId) {
        var caaEmails = OrganisationUtils.getCaaEmails(
            organisationService.findUsersInOrganisation(organisationId),
            EMAIL_LIMIT
        );
        if (caaEmails.isEmpty()) {
            caaEmails.add(getSuperUserEmail(organisationId));
        }
        return caaEmails;
    }

    private String getSuperUserEmail(String organisationId) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationId);
        if (!organisation.isPresent()) {
            throw new CallbackException("Organisation was not found");
        }
        return  organisation.get().getSuperUser().getEmail();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONSE_DEADLINE, formatLocalDate(caseData
                                                                     .getRespondent1ResponseDeadline()
                                                                     .toLocalDate(), DATE),
            RESPONSE_DEADLINE_PLUS_28, formatLocalDate(caseData
                                                                             .getRespondent1ResponseDeadline()
                                                                             .toLocalDate()
                                                                             .plusDays(28)
                                                                             .atTime(23, 59)
                                                                             .toLocalDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
