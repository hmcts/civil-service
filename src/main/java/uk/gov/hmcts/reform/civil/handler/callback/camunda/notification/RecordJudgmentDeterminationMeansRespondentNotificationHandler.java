package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class RecordJudgmentDeterminationMeansRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT,
                                                          NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT);
    public static final String TASK_ID_RESPONDENT1 = "RecordJudgmentNotifyRespondent1";
    public static final String TASK_ID_RESPONDENT2 = "RecordJudgmentNotifyRespondent2";
    private static final String REFERENCE_TEMPLATE = "record-judgment-determination-means-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForRecordJudgmentDeterminationOfMeans
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.equals(caseEvent)) {
            return TASK_ID_RESPONDENT1;
        } else {
            return TASK_ID_RESPONDENT2;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForRecordJudgmentDeterminationOfMeans(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isRespondentLip = caseData.isRespondent1NotRepresented();
        String emailTemplateID;
        if (isRespondentLip) {
            if (caseData.isRespondentResponseBilingual()) {
                emailTemplateID = notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                emailTemplateID = notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            emailTemplateID = notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate();
        }

        String recipient = isRespondentLip ? caseData.getRespondent1().getPartyEmail() :
            callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name())
                ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress();

        if (nonNull(recipient)) {
            notificationService.sendMail(
                recipient,
                emailTemplateID,
                isRespondentLip ? addPropertiesLip(caseData)
                    : callbackParams.getRequest().getEventId().equals(NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name())
                    ? addProperties(caseData) : addRespondent2Properties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, NotificationUtils.getRespondentLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy(),
                organisationService),
            DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData)
        );
    }

    public Map<String, String> addRespondent2Properties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, NotificationUtils.getRespondentLegalOrganizationName(
                caseData.getRespondent2OrganisationPolicy(),
                organisationService),
            DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData)
        );
    }

    private Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
        );
    }
}
