package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CONTACT_DETAILS_CHANGE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DefendantChangeOfAddressApplicantForCuiNotificationHandler
    extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_CONTACT_DETAILS_CHANGE
    );

    public static final String TASK_ID = "DefendantContactDetailsChangeNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "defendant-contact-details-change-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final PinInPostConfiguration pipInPostConfiguration;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifySolicitorsForContactDetailsChange
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

    private CallbackResponse notifySolicitorsForContactDetailsChange(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isLiPClaimant = caseData.isApplicantNotRepresented();

        notificationService.sendMail(
            isLiPClaimant ? caseData.getApplicant1Email() : caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getEmailTemplate(caseData, isLiPClaimant),
            isLiPClaimant ? addPropertiesForLiPClaimant(caseData) : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private Map<String, String> addPropertiesForLiPClaimant(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl(),
            EXTERNAL_ID, caseData.getCcdCaseReference().toString()
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData)
        );
    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        String id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    public String getEmailTemplate(CaseData caseData, boolean isLiPClaimant) {
        String emailTemplate;
        if (isLiPClaimant) {
            emailTemplate = notificationsProperties.getNotifyLiPClaimantDefendantChangedContactDetails();
        } else {
            emailTemplate =  notificationsProperties.getRespondentChangeOfAddressNotificationTemplate();
        }
        return emailTemplate;
    }
}
