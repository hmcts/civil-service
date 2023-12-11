package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@SpringBootTest(classes = {
    SendGenericNotificationLipService.class,
    JacksonAutoConfiguration.class,
})
public class SendGenericNotificationLipServiceTest {

    @Autowired
    private SendGenericNotificationLipService service;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setup() {
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(
            "test-template-received-id");
    }

    @Test
    void shouldSendGenericEmailWhenAllDataIsCorrect() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build()).build();

        service.sendGenericNotificationLip(caseData);

        verify(notificationService).sendMail(
            "claimant@hmcts.net",
            "test-template-received-id",
            getNotificationDataMap(caseData),
            "generic-notification-lip-000DC001"
        );

    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, "John Doe",
            CLAIMANT_V_DEFENDANT, "John Doe V Jack Jackson"
        );
    }
}
