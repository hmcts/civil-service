package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dj.DjNotificationPropertiesService;
import uk.gov.hmcts.reform.civil.service.dj.DjNotificationRecipientService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StandardDirectionOrderDJDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private StandardDirectionOrderDJDefendantNotificationHandler handler;

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private DjNotificationRecipientService recipientService;
    @Mock
    private DjNotificationPropertiesService propertiesService;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        when(notificationsProperties.getStandardDirectionOrderDJTemplate()).thenReturn("template-id-sdo");
    }

    @Test
    void shouldNotifyRespondent1WhenRecipientServiceAllowsIt() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .legacyCaseReference("000DC001")
            .build();
        final CallbackParams params = paramsForEvent(caseData, NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name());
        when(recipientService.shouldNotifyRespondent1(caseData)).thenReturn(true);
        when(recipientService.getRespondent1Email(caseData)).thenReturn("respondent1@example.com");
        Map<String, String> props = Map.of("legalOrgName", "Org1");
        when(propertiesService.buildDefendant1Properties(caseData)).thenReturn(props);

        handler.handle(params);

        verify(notificationService).sendMail(
            "respondent1@example.com",
            "template-id-sdo",
            props,
            "sdo-dj-order-notification-defendant-000DC001"
        );
    }

    @Test
    void shouldSkipRespondent1WhenRecipientServiceReturnsFalse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .legacyCaseReference("000DC001")
            .build();
        final CallbackParams params = paramsForEvent(caseData, NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name());
        when(recipientService.shouldNotifyRespondent1(caseData)).thenReturn(false);

        handler.handle(params);

        verify(notificationService, never()).sendMail(any(), any(), any(), any());
    }

    @Test
    void shouldNotifyRespondent2WhenRecipientServiceAllowsIt() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
            .build()
            .toBuilder()
            .legacyCaseReference("000DC001")
            .build();
        final CallbackParams params = paramsForEvent(caseData, NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2.name());
        when(recipientService.shouldNotifyRespondent2(caseData)).thenReturn(true);
        when(recipientService.getRespondent2Email(caseData)).thenReturn("respondent2@example.com");
        Map<String, String> props = Map.of("legalOrgName", "Org2");
        when(propertiesService.buildDefendant2Properties(caseData)).thenReturn(props);

        handler.handle(params);

        verify(notificationService).sendMail(
            "respondent2@example.com",
            "template-id-sdo",
            props,
            "sdo-dj-order-notification-defendant-000DC001"
        );
    }

    @Test
    void shouldHandleNotificationExceptionsGracefully() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .legacyCaseReference("000DC001")
            .build();
        final CallbackParams params = paramsForEvent(caseData, NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name());
        when(recipientService.shouldNotifyRespondent1(caseData)).thenReturn(true);
        when(recipientService.getRespondent1Email(caseData)).thenReturn("respondent1@example.com");
        Map<String, String> props = Map.of("legalOrgName", "Org");
        when(propertiesService.buildDefendant1Properties(caseData)).thenReturn(props);
        doThrow(new RuntimeException("failure")).when(notificationService)
            .sendMail(any(), any(), any(), any());

        handler.handle(params);

        verify(notificationService).sendMail(any(), any(), any(), any());
    }

    private CallbackParams paramsForEvent(CaseData caseData, String eventId) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder()
                         .eventId(eventId)
                         .caseDetails(CaseDetails.builder().id(1L).build())
                         .build())
            .build();
    }
}
