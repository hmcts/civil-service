package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class StandardDirectionOrderDJClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private StandardDirectionOrderDJClaimantNotificationHandler handler;

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
    void shouldNotifyClaimantUsingSharedServices() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        when(recipientService.getClaimantEmail(caseData)).thenReturn("claimant@example.com");
        Map<String, String> properties = Map.of("legalOrgName", "Org");
        when(propertiesService.buildClaimantProperties(caseData)).thenReturn(properties);

        handler.handle(params);

        verify(notificationService).sendMail(
            "claimant@example.com",
            "template-id-sdo",
            properties,
            "sdo-dj-order-notification-claimant-000DC001"
        );
    }
}
