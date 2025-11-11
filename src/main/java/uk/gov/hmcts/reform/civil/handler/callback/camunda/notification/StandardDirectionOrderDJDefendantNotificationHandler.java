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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJDefendantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(StandardDirectionOrderDJDefendantNotificationHandler.class);

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final DjNotificationRecipientService recipientService;
    private final DjNotificationPropertiesService propertiesService;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT,
        NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2
    );
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";
    private static final String TASK_ID_DEFENDANT = "StandardDirectionOrderDj";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantSDOrderDj
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_DEFENDANT;
    }

    private CallbackResponse notifyDefendantSDOrderDj(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        String eventId = callbackParams.getRequest().getEventId();

        if (eventId.equals(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name())) {
            if (recipientService.shouldNotifyRespondent1(caseData)) {
                sendNotification(
                    () -> recipientService.getRespondent1Email(caseData),
                    addProperties(caseData),
                    callbackParams
                );
            }
        } else if (eventId.equals(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2.name())) {
            if (recipientService.shouldNotifyRespondent2(caseData)) {
                sendNotification(
                    () -> recipientService.getRespondent2Email(caseData),
                    buildDefendant2Properties(caseData),
                    callbackParams
                );
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void sendNotification(EmailSupplier emailSupplier,
                                  Map<String, String> properties,
                                  CallbackParams callbackParams) {
        try {
            notificationService.sendMail(
                emailSupplier.get(),
                notificationsProperties.getStandardDirectionOrderDJTemplate(),
                properties,
                String.format(
                    REFERENCE_TEMPLATE_SDO_DJ,
                    callbackParams.getCaseData().getLegacyCaseReference()
                )
            );
        } catch (Exception e) {
            LOGGER.error("Failed to send DJ defendant notification for case {} due to error {}",
                         callbackParams.getRequest().getCaseDetails().getId(), e.getMessage());
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return propertiesService.buildDefendant1Properties(caseData);
    }

    private Map<String, String> buildDefendant2Properties(CaseData caseData) {
        return propertiesService.buildDefendant2Properties(caseData);
    }

    @FunctionalInterface
    private interface EmailSupplier {
        String get();
    }
}
