package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
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

@ExtendWith(MockitoExtension.class)
public class ClaimContinuingOnlineRespondentPartyForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private PiPLetterGenerator pipLetterGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private Time time;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ClaimContinuingOnlineRespondentPartyForSpecNotificationHandler handler;

    public static final String TASK_ID_Respondent1 = "CreateClaimContinuingOnlineNotifyRespondent1ForSpec";
    private static final byte[] LETTER_CONTENT = new byte[]{1, 2, 3, 4};

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            when(notificationsProperties.getRespondentDefendantResponseForSpec())
                .thenReturn("template-id");
            when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");

            // Given
            CaseData caseData = getCaseData("testorg@email.com");
            CallbackParams params = getCallbackParams(caseData);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            verify(notificationService).sendMail(
                "testorg@email.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
            assertThat(response.getState()).isEqualTo("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
        }

        @Test
        void shouldNotNotifyRespondent1Solicitor_whenNoEmailIsEntered() {
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
            given(pipLetterGenerator.downloadLetter(any(), any())).willReturn(LETTER_CONTENT);

            when(notificationsProperties.getRespondentDefendantResponseForSpec())
                .thenReturn("template-id");
            when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");

            // Given
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
                    Collections.singletonList(caseData.getRespondent1().getPartyName())
                );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                RESPONDENT_NAME, "Mr. Sole Trader",
                CLAIMANT_NAME, "Mr. John Rambo",
                ISSUED_ON, formatLocalDate(LocalDate.now(), DATE),
                RESPOND_URL, "dummy_respond_to_claim_url",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_16_DIGIT_NUMBER, CASE_ID.toString(),
                PIN, "TEST1234",
                RESPONSE_DEADLINE, formatLocalDate(
                    caseData.getRespondent1ResponseDeadline()
                        .toLocalDate(), DATE),
                FRONTEND_URL, "dummy_cui_front_end_url"
            );
        }

        @Test
        void shouldNotUpdateCaseState_whenBilingualSelectedAndR2EnabledForLipvsLip() {
            when(notificationsProperties.getRespondentDefendantResponseForSpec())
                .thenReturn("template-id");
            when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            // Given
            CaseData caseData = getCaseData("testorg@email.com");
            CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
            updatedCaseData.claimantBilingualLanguagePreference("BOTH")
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(123L).build();
            CaseData updatedData = updatedCaseData.build();
            CallbackParams params = getCallbackParams(updatedData);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Assertions
            assertThat(response.getState()).isEqualTo(caseData.getCcdState().name());
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC").build())
                                                 .build())).isEqualTo(TASK_ID_Respondent1);
    }

    private CallbackParams getCallbackParams(CaseData caseData) {
        return CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId("NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                .build()).build();
    }

    private CaseData getCaseData(String email) {
        return CaseDataBuilder.builder().atStateClaimNotified()
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
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    }
}
