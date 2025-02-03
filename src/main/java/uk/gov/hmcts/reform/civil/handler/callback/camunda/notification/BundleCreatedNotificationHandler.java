package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class BundleCreatedNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final OrganisationService organisationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED,
        CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED
    );
    public static final String TASK_ID_APPLICANT = "BundleCreationNotifyApplicantSolicitor1";
    public static final String TASK_ID_DEFENDANT1 = "BundleCreationNotifyRespondentSolicitor1";
    public static final String TASK_ID_DEFENDANT2 = "BundleCreationNotifyRespondentSolicitor2";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyBundleCreated
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED.name())) {
            return TASK_ID_APPLICANT;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name())) {
            return TASK_ID_DEFENDANT1;
        } else {
            return TASK_ID_DEFENDANT2;
        }
    }

    private CallbackResponse notifyBundleCreated(CallbackParams callbackParams) {
        String taskId = camundaActivityId(callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        boolean isSameRespondentSolicitor = getIsSameRespondentSolicitor(caseData, taskId);
        if (isSameRespondentSolicitor) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String emailAddress = getRecipientEmail(caseData, taskId);
        String template = getReferenceTemplateString(taskId);

        Map<String, String> properties = new HashMap<>();
        if (!isLip(caseData, taskId)) {
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getOrgName(caseData, CaseEvent.valueOf(callbackParams.getRequest().getEventId())));
        }

        if (nonNull(emailAddress)) {
            notificationService.sendMail(
                emailAddress,
                getTemplate(caseData, taskId),
                isLip(caseData, taskId) ? addPropertiesLip(caseData, taskId) : properties,
                String.format(template, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getTemplate(CaseData caseData, String taskId) {
        boolean isLipWelsh = false;
        boolean isLip = false;
        if (taskId.equals(TASK_ID_APPLICANT)) {
            isLip = isApplicant1Lip(caseData);
            isLipWelsh = isLip && caseData.isClaimantBilingual();
        } else if (taskId.equals(TASK_ID_DEFENDANT1)) {
            isLip = isRespondent1Lip(caseData);
            isLipWelsh = isLip && caseData.isRespondentResponseBilingual();
        }
        if (isLip) {
            if (isLipWelsh) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                return notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            return notificationsProperties.getBundleCreationTemplate();
        }
    }

    private String getReferenceTemplateString(String taskId) {
        if (taskId.equals(TASK_ID_APPLICANT)) {
            return "bundle-created-applicant-notification-%s";
        } else {
            return "bundle-created-respondent-notification-%s";
        }
    }

    private boolean getIsSameRespondentSolicitor(CaseData caseData, String taskId) {
        boolean isSameRespondentSolicitor = false;
        if (taskId.equals(TASK_ID_DEFENDANT2) && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            isSameRespondentSolicitor = true;
        }
        return isSameRespondentSolicitor;
    }

    private String getRecipientEmail(CaseData caseData, String taskId) {
        if (taskId.equals(TASK_ID_APPLICANT)) {
            if (isApplicant1Lip(caseData)) {
                return caseData.getApplicant1().getPartyEmail();
            }
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (taskId.equals(TASK_ID_DEFENDANT1)) {
            if (isRespondent1Lip(caseData)) {
                return caseData.getRespondent1().getPartyEmail();
            }
            return caseData.getRespondentSolicitor1EmailAddress();
        } else {
            return caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesLip(CaseData caseData, String taskId) {
        String partyName = taskId.equals(TASK_ID_APPLICANT) ? caseData.getApplicant1().getPartyName() : caseData.getRespondent1().getPartyName();
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            PARTY_NAME, partyName
        );
    }

    private boolean isRespondent1Lip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getRespondent1Represented()));
    }

    private boolean isApplicant1Lip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getApplicant1Represented()));
    }

    private boolean isLip(CaseData caseData, String taskId) {
        if (taskId.equals(TASK_ID_APPLICANT)) {
            return isApplicant1Lip(caseData);
        } else if (taskId.equals(TASK_ID_DEFENDANT1)) {
            return isRespondent1Lip(caseData);
        } else {
            return false;
        }
    }

    private String getOrgName(CaseData caseData, CaseEvent caseEvent) {
        return switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED -> getApplicantLegalOrganizationName(
                caseData,
                organisationService
            );
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED -> getRespondentLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy(),
                organisationService
            );
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED -> getRespondentLegalOrganizationName(
                caseData.getRespondent2OrganisationPolicy(),
                organisationService
            );
            default -> throw new IllegalArgumentException("Invalid case event in " + caseEvent);
        };
    }
}
