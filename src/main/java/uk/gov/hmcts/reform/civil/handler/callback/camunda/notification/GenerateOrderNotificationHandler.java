package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
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

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    private final OrganisationService organisationService;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_COURT_OFFICER_ORDER
    );
    public static final String TASK_ID_APPLICANT = "GenerateOrderNotifyApplicantSolicitor1";
    public static final String TASK_ID_APPLICANT_COURT_OFFICER_ORDER = "GenerateOrderNotifyApplicantCourtOfficerOrderSolicitor1";
    public static final String TASK_ID_RESPONDENT_COURT_OFFICER_ORDER = "GenerateOrderNotifyRespondentCourtOfficerOrderSolicitor1";
    public static final String TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER = "GenerateOrderNotifyRespondentCourtOfficerOrderSolicitor2";
    public static final String TASK_ID_RESPONDENT1 = "GenerateOrderNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "GenerateOrderNotifyRespondentSolicitor2";

    public String taskId = "";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyBundleCreated
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER.name())) {
            return TASK_ID_APPLICANT;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name())) {
            return TASK_ID_APPLICANT_COURT_OFFICER_ORDER;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name())) {
            return TASK_ID_RESPONDENT_COURT_OFFICER_ORDER;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT_SOLICITOR2_FOR_COURT_OFFICER_ORDER.name())) {
            return TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER.name())) {
            return TASK_ID_RESPONDENT1;
        } else {
            return TASK_ID_RESPONDENT2;
        }
    }

    private CallbackResponse notifyBundleCreated(CallbackParams callbackParams) {
        taskId = camundaActivityId(callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        boolean isSameRespondentSolicitor = getIsSameRespondentSolicitor(caseData);
        if (isSameRespondentSolicitor) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String emailAddress = getReceipientEmail(caseData);
        if (StringUtils.isNotBlank(emailAddress)) {
            String template = getReferenceTemplateString();
            notificationService.sendMail(
                emailAddress,
                getTemplate(caseData),
                addProperties(caseData),
                String.format(template, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getTemplate(CaseData caseData) {
        boolean isLip = false;
        boolean isLipWelsh = false;
        boolean isCourtOfficerOrderLipWelsh = false;
        if (isApplicantLip(caseData) && taskId.equals(TASK_ID_APPLICANT)) {
            isLip = true;
            isLipWelsh = caseData.isClaimantBilingual();
        } else if (isApplicantLip(caseData) && taskId.equals(TASK_ID_APPLICANT_COURT_OFFICER_ORDER)) {
            isLip = true;
            isLipWelsh = caseData.isClaimantBilingual();
            isCourtOfficerOrderLipWelsh = true;
        } else if (isRespondent1Lip(caseData) && taskId.equals(TASK_ID_RESPONDENT_COURT_OFFICER_ORDER)) {
            isLip = true;
            isLipWelsh = caseData.isRespondentResponseBilingual();
            isCourtOfficerOrderLipWelsh = true;
        } else if (isRespondent1Lip(caseData) && taskId.equals(TASK_ID_RESPONDENT1)) {
            isLip = true;
            isLipWelsh = caseData.isRespondentResponseBilingual();
        } else if (isRespondent2Lip(caseData) && taskId.equals(TASK_ID_RESPONDENT2_COURT_OFFICER_ORDER)) {
            isLip = true;
            isLipWelsh = caseData.isClaimantBilingual();
            isCourtOfficerOrderLipWelsh = true;
        } else if (isRespondent2Lip(caseData) && taskId.equals(TASK_ID_RESPONDENT2)) {
            // TODO CIV-13814 not contemplated response 2 currently
            isLip = true;
        }
        if (isLip) {
            if (isLipWelsh && isCourtOfficerOrderLipWelsh) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else if (isLipWelsh) {
                return notificationsProperties.getOrderBeingTranslatedTemplateWelsh();
            } else {
                return notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            return notificationsProperties.getGenerateOrderNotificationTemplate();
        }
    }

    private String getReferenceTemplateString() {
        return "generate-order-notification-%s";
    }

    private boolean getIsSameRespondentSolicitor(CaseData caseData) {
        return taskId.equals(TASK_ID_RESPONDENT2) && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES;
    }

    private String getReceipientEmail(CaseData caseData) {
        if (taskId.equals(TASK_ID_APPLICANT) || taskId.equals(TASK_ID_APPLICANT_COURT_OFFICER_ORDER)) {
            return isApplicantLip(caseData)
                ? caseData.getApplicant1().getPartyEmail() : caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (taskId.equals(TASK_ID_RESPONDENT1) || taskId.equals(TASK_ID_RESPONDENT_COURT_OFFICER_ORDER)) {
            return isRespondent1Lip(caseData)
                ? caseData.getRespondent1().getPartyEmail() : caseData.getRespondentSolicitor1EmailAddress();
        } else {
            return isRespondent2Lip(caseData)
                ? caseData.getRespondent2().getPartyEmail() : caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public String getLegalOrganizationName(CaseData caseData) {

        String id = "";
        if (taskId.equals(TASK_ID_APPLICANT) || taskId.equals(TASK_ID_APPLICANT_COURT_OFFICER_ORDER)) {
            id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else if (taskId.equals(TASK_ID_RESPONDENT1) || taskId.equals(TASK_ID_RESPONDENT_COURT_OFFICER_ORDER)) {
            id = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            id = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }

        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    public String getPartyName(CaseData caseData) {
        if (taskId.equals(TASK_ID_APPLICANT) || taskId.equals(TASK_ID_APPLICANT_COURT_OFFICER_ORDER)) {
            return caseData.getApplicant1().getPartyName();
        } else if (taskId.equals(TASK_ID_RESPONDENT1) || taskId.equals(TASK_ID_RESPONDENT_COURT_OFFICER_ORDER)) {
            return caseData.getRespondent1().getPartyName();
        } else {
            return caseData.getRespondent2().getPartyName();
        }
    }

    public Map<String, String> addProperties(CaseData caseData) {
        if (taskId.equals(TASK_ID_APPLICANT) || taskId.equals(TASK_ID_APPLICANT_COURT_OFFICER_ORDER)) {
            return isApplicantLip(caseData) ? getLipProperties(caseData) : getLRProperties(caseData);
        } else if (taskId.equals(TASK_ID_RESPONDENT1) || taskId.equals(TASK_ID_RESPONDENT_COURT_OFFICER_ORDER)) {
            return isRespondent1Lip(caseData) ? getLipProperties(caseData) : getLRProperties(caseData);
        } else {
            return isRespondent2Lip(caseData) ? getLipProperties(caseData) : getLRProperties(caseData);
        }
    }

    private Map<String, String> getLRProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationName(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    private Map<String, String> getLipProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, getPartyName(caseData),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    private boolean isApplicantLip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getApplicant1Represented()));
    }

    private boolean isRespondent1Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    private boolean isRespondent2Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent2Represented());
    }
}
