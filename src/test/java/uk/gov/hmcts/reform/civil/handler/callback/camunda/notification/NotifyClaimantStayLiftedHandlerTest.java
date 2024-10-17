package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_LIFTED;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantStayLiftedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyClaimantStayLiftedHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    @Test
    void checkCamundaActivityTest() {
        caseData = caseData.toBuilder().build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyClaimantStayLifted", response);
    }

    @Test
    void checkHandleEventTest() {
        var response = handler.handledEvents();
        assertEquals(List.of(NOTIFY_CLAIMANT_STAY_LIFTED), response);
    }

    @Test
    void sendNotificationShouldSendEmail() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();

        when(notificationsProperties.getNotifyLRStayLifted()).thenReturn("solicitor-template");

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            "respondentSolicitor@hmcts.net",
            "solicitor-template",
            Map.of(
                "claimantvdefendant" , "John Doe V Jack Jackson",
                "claimReferenceNumber", "1594901956117591",
                "name", "John Doe"
            ),
            "stay-lifted-claimant-notification-1594901956117591"
        );
        assertNotNull(response);
    }
}
