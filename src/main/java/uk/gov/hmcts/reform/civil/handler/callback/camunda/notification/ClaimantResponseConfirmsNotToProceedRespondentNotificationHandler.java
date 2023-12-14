package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC);

    public static final String TASK_ID = "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor1";
    public static final String Task_ID_RESPONDENT_SOL2 = "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor2";
    public static final String TASK_ID_CC = "ClaimantConfirmsNotToProceedNotifyApplicantSolicitor1CC";
    private static final String REFERENCE_TEMPLATE = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimantConfirmsNotToProceed
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (isRespondentSolicitor2Notification(callbackParams)) {
            return Task_ID_RESPONDENT_SOL2;
        }
        return isCcNotification(callbackParams) ? TASK_ID_CC : TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimantConfirmsNotToProceed(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        var recipient = isCcNotification(callbackParams)
            ? caseData.getApplicantSolicitor1UserDetails().getEmail()
            : caseData.getRespondentSolicitor1EmailAddress();

        if (isRespondentSolicitor2Notification(callbackParams)) {
            recipient = caseData.getRespondentSolicitor2EmailAddress();
        }

        notificationService.sendMail(
            recipient,
            getTemplate(caseData, callbackParams),
            SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? addPropertiesSpec(caseData, caseEvent)
                : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getTemplate(CaseData caseData, CallbackParams callbackParams) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (caseData.isPartAdmitPayImmediatelyAccepted()) {
                return notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec();
            } else {
                return isCcNotification(callbackParams)
                    ? notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpec()
                    : notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec();
            }
        } else {
            return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData, CaseEvent caseEvent) {
        if (caseData.isPartAdmitPayImmediatelyAccepted()) {
            return Map.of(
                CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    private String getRespondentLegalOrganizationName(CaseData caseData) {
        String id = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);

        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC.name());
    }

    private boolean isRespondentSolicitor2Notification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED.name());
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData,  CaseEvent caseEvent) {
        String organisationID;
        if (caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC)) {
            organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            organisationID = caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED)
                ? caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()
                : caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        }
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
