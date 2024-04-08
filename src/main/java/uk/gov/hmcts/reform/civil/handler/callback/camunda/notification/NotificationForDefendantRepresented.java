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
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED;

@Service
@RequiredArgsConstructor
public class NotificationForDefendantRepresented extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_LIP =
        "notify-lip-after-defendant-noc-approval-%s";
    private static final String REFERENCE_TEMPLATE_LR =
        "notify-lr-after-defendant-noc-approval-%s";
    public static final String TASK_ID_DEFENDANT = "NotifyDefendantLipAfterNocApproval";
    public static final String TASK_ID_DEFENDANT_LR = "NotifyDefendantLrAfterNocApproval";
    public static final String TASK_ID_CLAIMANT = "NotifyClaimantLipDefendantRepresented";
    public static final String TASK_ID_CLAIMANT_LR = "NotifyClaimantLrDefendantRepresented";
    private final String TEMPLATE_MAP_ID = "template-id";
    private final String EMAIL_MAP_ID = "email-id";
    private final String PROPERTIES_MAP_ID = "properties-id";
    private final String REFERENCE_MAP_ID = "reference-id";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyLipAfterNocApproval
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return setNotificationCamundaActivity(callbackParams);
    }

    private CallbackResponse notifyLipAfterNocApproval(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Map<String, Object> notificationTemplateMapping = setNotificationMapping(callbackParams);
        if (notificationTemplateMapping != null && isNotEmpty(notificationTemplateMapping.get(EMAIL_MAP_ID).toString())) {
            notificationService.sendMail(
                notificationTemplateMapping.get(EMAIL_MAP_ID).toString(),
                notificationTemplateMapping.get(TEMPLATE_MAP_ID).toString(),
                (Map<String, String>) notificationTemplateMapping.get(PROPERTIES_MAP_ID),
                String.format(notificationTemplateMapping.get(REFERENCE_MAP_ID).toString(), caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(
            NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL,
            NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL,
            NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    private Map<String, Object> setNotificationMapping(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL.name())) {
            mapping.put(TEMPLATE_MAP_ID, notificationsProperties.getNotifyDefendantLipForNoLongerAccessTemplate());
            mapping.put(EMAIL_MAP_ID, caseData.getRespondent1Email());
            mapping.put(PROPERTIES_MAP_ID, properties);
            mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_LIP);
            return mapping;
        } else if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL.name())) {
            mapping.put(TEMPLATE_MAP_ID, notificationsProperties.getNotifyDefendantLrAfterNoticeOfChangeTemplate());
            mapping.put(EMAIL_MAP_ID, caseData.getRespondent1Email());
            mapping.put(PROPERTIES_MAP_ID, properties);
            mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_LR);
            return mapping;
        } else if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED.name())) {
            if (caseData.getApplicant1Represented() == YesOrNo.NO) {
                mapping.put(TEMPLATE_MAP_ID, notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate());
                mapping.put(PROPERTIES_MAP_ID, properties);
                mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_LIP);
            } else {
                mapping.put(TEMPLATE_MAP_ID, notificationsProperties.getNoticeOfChangeOtherParties());
                mapping.put(PROPERTIES_MAP_ID, properties);
                mapping.put(REFERENCE_MAP_ID, REFERENCE_TEMPLATE_LR);
            }
            mapping.put(EMAIL_MAP_ID, caseData.getApplicant1Email());
            return mapping;
        }
        return null;
    }

    private String setNotificationCamundaActivity(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL.name())) {
            return TASK_ID_DEFENDANT;
        } else if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL.name())) {
            return TASK_ID_DEFENDANT_LR;
        } else if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED.name())) {
            if (caseData.getApplicant1Represented() == YesOrNo.NO) {
                return TASK_ID_CLAIMANT;
            }
            return TASK_ID_CLAIMANT_LR;
        }
        return null;
    }
}
