package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class GenerateOrderNotificationHandler extends CallbackHandler implements NotificationData {

    public static final String TASK_ID_APPLICANT = "GenerateOrderNotifyApplicantSolicitor1";
    public static final String TASK_ID_APPLICANT_COURT_OFFICER_ORDER = "GenerateOrderNotifyApplicantCourtOfficerOrderSolicitor1";
    public static final String TASK_ID_RESPONDENT_COURT_OFFICER_ORDER = "GenerateOrderNotifyRespondentCourtOfficerOrderSolicitor1";
    public static final String TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER = "GenerateOrderNotifyRespondentCourtOfficerOrderSolicitor2";
    public static final String TASK_ID_RESPONDENT1 = "GenerateOrderNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "GenerateOrderNotifyRespondentSolicitor2";

    private static final String REFERENCE_TEMPLATE = "generate-order-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private final OrganisationService organisationService;

    private static final Map<CaseEvent, String> EVENT_TASK_ID_MAP = Map.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER, TASK_ID_APPLICANT,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER, TASK_ID_APPLICANT_COURT_OFFICER_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER, TASK_ID_RESPONDENT_COURT_OFFICER_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_COURT_OFFICER_ORDER, TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER, TASK_ID_RESPONDENT1,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER, TASK_ID_RESPONDENT2
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyParty
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.copyOf(EVENT_TASK_ID_MAP.keySet());
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return Optional.ofNullable(callbackParams.getRequest().getEventId())
            .map(eventId -> {
                try {
                    return CaseEvent.valueOf(eventId);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .map(EVENT_TASK_ID_MAP::get)
            .orElse(TASK_ID_RESPONDENT2);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of();
    }

    private CallbackResponse notifyParty(CallbackParams callbackParams) {
        String taskId = camundaActivityId(callbackParams);
        CaseData caseData = callbackParams.getCaseData();

        if (isSameRespondentSolicitor(caseData, taskId)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        PartyContext party = getPartyContext(caseData, taskId);
        String recipientEmail = party.isLiP() ? party.lipEmail() : party.lrEmail();

        if (StringUtils.isNotBlank(recipientEmail)) {
            notificationService.sendMail(
                recipientEmail,
                determineTemplate(caseData, taskId, party.isLiP()),
                createNotificationProperties(caseData, party),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String determineTemplate(CaseData caseData, String taskId, boolean isLiP) {
        if (!isLiP) {
            return notificationsProperties.getGenerateOrderNotificationTemplate();
        }

        boolean isBilingual = isPartyBilingual(caseData, taskId);
        boolean isCourtOfficerOrder = isCourtOfficerOrderTask(taskId);

        if (isBilingual && isCourtOfficerOrder) {
            return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
        } else if (isBilingual) {
            return notificationsProperties.getOrderBeingTranslatedTemplateWelsh();
        } else {
            return notificationsProperties.getNotifyLipUpdateTemplate();
        }
    }

    private Map<String, String> createNotificationProperties(CaseData caseData, PartyContext party) {
        Map<String, String> properties = new HashMap<>(getCommonProperties(caseData));

        if (party.isLiP()) {
            properties.put(PARTY_NAME, party.name());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, resolveLegalOrganizationName(caseData, party.orgPolicy()));
        }

        return properties;
    }

    private Map<String, String> getCommonProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private String resolveLegalOrganizationName(CaseData caseData, OrganisationPolicy policy) {
        return Optional.ofNullable(policy)
            .map(OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
            .flatMap(organisationService::findOrganisationById)
            .map(uk.gov.hmcts.reform.civil.prd.model.Organisation::getName)
            .orElseGet(() -> caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    private boolean isSameRespondentSolicitor(CaseData caseData, String taskId) {
        return TASK_ID_RESPONDENT2.equals(taskId) && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES;
    }

    private boolean isCourtOfficerOrderTask(String taskId) {
        return taskId.equals(TASK_ID_APPLICANT_COURT_OFFICER_ORDER)
            || taskId.equals(TASK_ID_RESPONDENT_COURT_OFFICER_ORDER)
            || taskId.equals(TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER);
    }

    private boolean isPartyBilingual(CaseData caseData, String taskId) {
        return switch (taskId) {
            case TASK_ID_APPLICANT, TASK_ID_APPLICANT_COURT_OFFICER_ORDER, TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER ->
                caseData.isClaimantBilingual();
            case TASK_ID_RESPONDENT1, TASK_ID_RESPONDENT_COURT_OFFICER_ORDER ->
                caseData.isRespondentResponseBilingual();
            default -> false;
        };
    }

    private record PartyContext(boolean isLiP, String lipEmail, String lrEmail, String name, OrganisationPolicy orgPolicy) {}

    private PartyContext getPartyContext(CaseData caseData, String taskId) {
        return switch (taskId) {
            case TASK_ID_APPLICANT, TASK_ID_APPLICANT_COURT_OFFICER_ORDER -> new PartyContext(
                isLiP(caseData.getApplicant1Represented()),
                getPartyEmail(caseData.getApplicant1()),
                getApplicantSolicitorEmail(caseData),
                getPartyName(caseData.getApplicant1()),
                caseData.getApplicant1OrganisationPolicy()
            );
            case TASK_ID_RESPONDENT1, TASK_ID_RESPONDENT_COURT_OFFICER_ORDER -> new PartyContext(
                isLiP(caseData.getRespondent1Represented()),
                getPartyEmail(caseData.getRespondent1()),
                caseData.getRespondentSolicitor1EmailAddress(),
                getPartyName(caseData.getRespondent1()),
                caseData.getRespondent1OrganisationPolicy()
            );
            case TASK_ID_RESPONDENT2, TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER -> new PartyContext(
                isLiP(caseData.getRespondent2Represented()),
                getPartyEmail(caseData.getRespondent2()),
                caseData.getRespondentSolicitor2EmailAddress(),
                getPartyName(caseData.getRespondent2()),
                caseData.getRespondent2OrganisationPolicy()
            );
            default -> new PartyContext(false, null, null, null, null);
        };
    }

    private boolean isLiP(YesOrNo represented) {
        return YesOrNo.NO.equals(represented);
    }

    private String getPartyEmail(Party party) {
        return Optional.ofNullable(party).map(Party::getPartyEmail).orElse(null);
    }

    private String getPartyName(Party party) {
        return Optional.ofNullable(party).map(Party::getPartyName).orElse(null);
    }

    private String getApplicantSolicitorEmail(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicantSolicitor1UserDetails())
            .map(IdamUserDetails::getEmail)
            .orElse(null);
    }
}
