package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ClaimContinuingOnlineApplicantPartyForSpecNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
public class ClaimContinuingOnlineApplicantPartyForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService toggleService;
    @MockBean
    private Time time;

    @Autowired
    private ClaimContinuingOnlineApplicantPartyForSpecNotificationHandler handler;

    public static final String TASK_ID_Applicant1 = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";

    @Nested
    class AboutToSubmitCallback {
        private LocalDateTime responseDeadline;

        @BeforeEach
        void setup() {
            responseDeadline = LocalDateTime.now().plusDays(14);
            when(notificationsProperties.getClaimantClaimContinuingOnlineForSpec())
                .thenReturn("template-id");
            when(deadlinesCalculator.plus14DaysDeadline(any())).thenReturn(responseDeadline);
        }

        @Test
        void shouldNotifyApplicant1PartyEmail_whenInvoked() {
            // Given
            CaseData caseData = getCaseData("testorg@email.com", null);
            CallbackParams params = getCallbackParams(caseData);

            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                "testorg@email.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicant1_UserDetailsEmail_whenInvoked() {
            // Given
            CaseData caseData = getCaseData(null, "testorg@email.com");
            CallbackParams params = getCallbackParams(caseData);

            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                "testorg@email.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                RESPONDENT_NAME, "Mr. Sole Trader",
                CLAIMANT_NAME, "Mr. John Rambo",
                ISSUED_ON, formatLocalDate(LocalDate.now(), DATE),
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                RESPONSE_DEADLINE, formatLocalDate(
                    caseData.getRespondent1ResponseDeadline()
                        .toLocalDate(), DATE)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC").build())
                                                 .build())).isEqualTo(TASK_ID_Applicant1);
    }

    private CallbackParams getCallbackParams(CaseData caseData) {
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId("NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                .build()).build();
        return params;
    }

    private CaseData getCaseData(String partyEmail, String claimantUserEmail) {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build().toBuilder()
            .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                            .partyEmail(partyEmail)
                            .build())
            .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                             .build())
            .claimDetailsNotificationDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.now())
            .addRespondent2(YesOrNo.NO)
            .claimantUserDetails(IdamUserDetails.builder().email(claimantUserEmail).build())
            .build();

        return caseData;
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    }
}
