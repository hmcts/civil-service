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
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PIN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPOND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ClaimContinuingOnlineRespondentPartyForSpecNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
public class ClaimContinuingOnlineRespondentPartyForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private PinInPostConfiguration pinInPostConfiguration;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private PiPLetterGenerator pipLetterGenerator;

    @MockBean
    private Time time;

    @Autowired
    private ClaimContinuingOnlineRespondentPartyForSpecNotificationHandler handler;

    public static final String TASK_ID_Respondent1 = "CreateClaimContinuingOnlineNotifyRespondent1ForSpec";
    private static final byte[] LETTER_CONTENT = new byte[]{1, 2, 3, 4};

    @Nested
    class AboutToSubmitCallback {
        private LocalDateTime responseDeadline;

        @BeforeEach
        void setup() {
            responseDeadline = LocalDateTime.now().plusDays(14);
            when(notificationsProperties.getRespondentDefendantResponseForSpec())
                .thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("test solicitor").build()));
            when(deadlinesCalculator.plus14DaysDeadline(any())).thenReturn(responseDeadline);
            when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
        }

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            // Given
            CaseData caseData = getCaseData("testorg@email.com");
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
        void shouldNotNotifyRespondent1Solicitor_whenNoEmailiIsEntered() {
            // Given
            CaseData caseData = getCaseData(null);
            CallbackParams params = getCallbackParams(caseData);

            // When
            handler.handle(params);

            // Then
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void shouldGenerateAndPrintLetterSuccessfully() {
            // Given
            given(pipLetterGenerator.downloadLetter(any())).willReturn(LETTER_CONTENT);
            CaseData caseData = getCaseData("testorg@email.com");
            CallbackParams params = getCallbackParams(caseData);

            // When
            handler.handle(params);

            // Then
            verify(bulkPrintService)
                .printLetter(
                    LETTER_CONTENT,
                    caseData.getLegacyCaseReference(),
                    caseData.getLegacyCaseReference(),
                    "first-contact-pack",
                    Arrays.asList(caseData.getRespondent1().getPartyName())
                );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                RESPONDENT_NAME, "Mr. Sole Trader",
                CLAIMANT_NAME, "Mr. John Rambo",
                ISSUED_ON, formatLocalDate(LocalDate.now(), DATE),
                RESPOND_URL, "dummy_respond_to_claim_url",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                PIN, "TEST1234",
                RESPONSE_DEADLINE, formatLocalDate(
                    caseData.getRespondent1ResponseDeadline()
                        .toLocalDate(), DATE),
                FRONTEND_URL, "dummy_cui_front_end_url"
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC").build())
                                                 .build())).isEqualTo(TASK_ID_Respondent1);
    }

    private CallbackParams getCallbackParams(CaseData caseData) {
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId("NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                .build()).build();
        return params;
    }

    private CaseData getCaseData(String email) {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
            .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                             .partyEmail(email)
                             .build())
            .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                               .accessCode("TEST1234")
                                               .expiryDate(LocalDate.now().plusDays(180))
                                               .build())
            .claimDetailsNotificationDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.now())
            .addRespondent2(YesOrNo.NO)
            .build();
        return caseData;
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    }

}
