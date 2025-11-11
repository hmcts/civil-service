package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.service.dj.DjNotificationPropertiesService;
import uk.gov.hmcts.reform.civil.service.dj.DjNotificationRecipientService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJClaimantNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final DjNotificationRecipientService recipientService;
    private final DjNotificationPropertiesService propertiesService;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT);
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-claimant-%s";
    private static final String TASK_ID_CLAIMANT = "StandardDirectionOrderDj";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantSDOrderDj
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyClaimantSDOrderDj(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        notificationService.sendMail(
            recipientService.getClaimantEmail(caseData),
            notificationsProperties.getStandardDirectionOrderDJTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE_SDO_DJ, caseData.getLegacyCaseReference()));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return propertiesService.buildClaimantProperties(caseData);
    }
}
