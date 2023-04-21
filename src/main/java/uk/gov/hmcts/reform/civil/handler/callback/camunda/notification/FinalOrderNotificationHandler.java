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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_FINAL_ORDER;


@Service
@RequiredArgsConstructor
public class FinalOrderNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    private final OrganisationService organisationService;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_FINAL_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_FINAL_ORDER,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_FINAL_ORDER
    );
    public static final String TASK_ID_APPLICANT = "FinalOrderNotifyApplicantSolicitor1";
    public static final String TASK_ID_DEFENDANT1 = "FinalOrderNotifyRespondentSolicitor1";
    public static final String TASK_ID_DEFENDANT2 = "FinalOrderNotifyRespondentSolicitor2";

    public String taskId = "";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyBundleCreated
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_FINAL_ORDER.name())) {
            return TASK_ID_APPLICANT;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_FINAL_ORDER.name())) {
            return TASK_ID_DEFENDANT1;
        } else {
            return TASK_ID_DEFENDANT2;
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
        String template = getReferenceTemplateString();
        notificationService.sendMail(
            emailAddress,
            notificationsProperties.getFinalOrderNotificationTemplate(),
            addProperties(caseData),
            String.format(template, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getReferenceTemplateString() {
            return "final-order-notification-%s";
    }

    private boolean getIsSameRespondentSolicitor(CaseData caseData) {
        boolean isSameRespondentSolicitor = false;
        if (taskId.equals(TASK_ID_DEFENDANT2) && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            isSameRespondentSolicitor = true;
        }
        return isSameRespondentSolicitor;
    }

    private String getReceipientEmail(CaseData caseData) {
        if (taskId.equals(TASK_ID_APPLICANT)) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (taskId.equals(TASK_ID_DEFENDANT1)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        } else {
            return caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public String getLegalOrganizationName(CaseData caseData) {

        String id = "";
        if(taskId == "FinalOrderNotifyApplicantSolicitor1"){
            id = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else if(taskId == "FinalOrderNotifyRespondentSolicitor1") {
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

    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationName(caseData)
        );
    }
}
