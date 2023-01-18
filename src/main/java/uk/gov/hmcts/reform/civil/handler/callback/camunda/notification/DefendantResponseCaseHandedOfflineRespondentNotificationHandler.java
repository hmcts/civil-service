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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;

@Service
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE
    );
    public static final String TASK_ID_RESPONDENT1 = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(
            callbackParams,
            NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE
        ) ? TASK_ID_RESPONDENT1
            : TASK_ID_RESPONDENT2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //Offline notification will point to a new MP template for displaying defendant responses
    private CallbackResponse notifyRespondentSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();
        String recipient;
        String templateID;

        //Use 1v1 Template
        if (is1v1Or2v1Case(caseData)) {
            recipient = caseData.getRespondentSolicitor1EmailAddress();
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            //Use Multiparty Template as there are 2 defendant responses
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                && !RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && !RespondentResponseTypeSpec.COUNTER_CLAIM
                .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                && isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled())) {
                templateID = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
            } else {
                templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
            }
            if (isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE)) {
                recipient = caseData.getRespondentSolicitor1EmailAddress();
            } else {
                recipient = caseData.getRespondentSolicitor2EmailAddress();
            }

            if (null == recipient && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
                recipient = caseData.getRespondentSolicitor1EmailAddress();
            }
        }

        if (isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame()))) {
                sendNotificationToSolicitorSpecCounterClaim(caseData, recipient, caseEvent);
            } else if (MultiPartyScenario.getMultiPartyScenario(caseData)
                .equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                && (caseData.getRespondent1ResponseDate() == null || caseData.getRespondent2ResponseDate() == null
                || caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE))) {
                sendNotificationToSolicitorSpec(caseData, recipient, caseEvent);
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
        return NotificationUtils.caseOfflineNotificationAddProperties(
            caseData, featureToggleService.isAccessProfilesEnabled());
    }

    private void sendNotificationToSolicitorSpecCounterClaim(CaseData caseData,
                                                             String recipient, CaseEvent caseEvent) {
        String emailTemplate = notificationsProperties.getRespondentSolicitorCounterClaimForSpec();
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec(caseData, caseEvent),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private void sendNotificationToSolicitorSpec(CaseData caseData,
                                                 String recipient, CaseEvent caseEvent) {
        String emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec1v2DiffSol(caseData, caseEvent),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData, CaseEvent caseEvent) {
        return Map.of(
            DEFENDANT_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesSpec1v2DiffSol(CaseData caseData, CaseEvent caseEvent) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private String getLegalOrganisationName(CaseData caseData, CaseEvent caseEvent) {
        String organisationID;
        organisationID = caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE)
            ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
