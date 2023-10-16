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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Service
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineApplicantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE);

    public static final String TASK_ID_APPLICANT1 = "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "defendant-response-case-handed-offline-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_APPLICANT1;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        String templateID;

        if (is1v1Or2v1Case(caseData)) {
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)
                && SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                templateID = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
            } else {
                templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
            }
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame()))) {
                sendNotificationToSolicitorSpecCounterClaim(caseData, recipient);
            } else if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                sendNotificationToSolicitorSpec(caseData, recipient);
            } else {
                sendNotificationToSolicitor(caseData, recipient, templateID);
            }
        } else {
            sendNotificationToSolicitor(caseData, recipient, templateID);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipient, String templateID) {
        notificationService.sendMail(
            recipient,
            templateID,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return NotificationUtils.caseOfflineNotificationAddProperties(caseData);
    }

    private void sendNotificationToSolicitorSpecCounterClaim(CaseData caseData, String recipient) {
        String emailTemplate = notificationsProperties.getClaimantSolicitorCounterClaimForSpec();
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private void sendNotificationToSolicitorSpec(CaseData caseData, String recipient) {
        String emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec1v2DiffSol(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData) {
        return Map.of(
            CLAIM_NAME_SPEC, getLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesSpec1v2DiffSol(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private String getLegalOrganisationName(CaseData caseData) {
        String organisationID;
        organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
