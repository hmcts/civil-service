package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_LIFTED;

@ExtendWith(MockitoExtension.class)
class NotifyDefendantStayLiftedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyDefendantStayLiftedHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
            .respondent2(Party.builder().individualFirstName("Jim").individualLastName("Jameson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(false, NOTIFY_DEFENDANT_STAY_LIFTED),
            Arguments.of(true, NOTIFY_DEFENDANT2_STAY_LIFTED)
        );
    }

    @Test
    void checkCamundaActivityDefendantTest() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_STAY_LIFTED.toString()).build()).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyDefendantStayLifted", response);
    }

    @Test
    void checkCamundaActivityDefendant2Test() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT2_STAY_LIFTED.toString()).build()).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyDefendant2StayLifted", response);
    }

    @Test
    void checkHandleEventTest() {
        var response = handler.handledEvents();
        assertEquals(List.of(NOTIFY_DEFENDANT_STAY_LIFTED,
                             NOTIFY_DEFENDANT2_STAY_LIFTED), response);
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void sendNotificationShouldSendEmail(boolean isDefendant2, CaseEvent caseEvent) {
        caseData = caseData.toBuilder()
            .respondentSolicitor1EmailAddress("defendant@hmcts.net")
            .respondentSolicitor2EmailAddress("defendant2@hmcts.net")
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(caseEvent.toString()).build()).build();

        when(notificationsProperties.getNotifyLRStayLifted()).thenReturn("solicitor-template");

        CallbackResponse response = handler.sendNotification(params);

        if (isDefendant2) {
            verify(notificationService).sendMail(
                "defendant2@hmcts.net",
                "solicitor-template",
                Map.of(
                    "claimReferenceNumber", "1594901956117591",
                    "name", "Jim Jameson"
                ),
                "stay-lifted-defendant-notification-1594901956117591"
            );
        } else {
            verify(notificationService).sendMail(
                "defendant@hmcts.net",
                "solicitor-template",
                Map.of(
                    "claimReferenceNumber", "1594901956117591",
                    "name", "Jack Jackson"
                ),
                "stay-lifted-defendant-notification-1594901956117591"
            );
        }
        assertNotNull(response);
    }

}
