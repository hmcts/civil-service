package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ResetPinCUICallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class ResetPinCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @Autowired
    private ResetPinCUICallbackHandler handler;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private PinInPostConfiguration pinInPostConfiguration;

    @Nested
    class AboutToSubmitCallback {

        private final DefendantPinToPostLRspec givenPin =
            DefendantPinToPostLRspec.builder()
                .expiryDate(LocalDate.of(
                                2021,
                                1,
                                1
                            )
                )
                .citizenCaseRole("citizen")
                .respondentCaseRole("citizen")
                .accessCode("123").build();
        private final DefendantPinToPostLRspec pin =
            DefendantPinToPostLRspec.builder()
                .expiryDate(LocalDate.now())
                .citizenCaseRole("citizen")
                .respondentCaseRole("citizen")
                .accessCode("123").build();

        @Test
        void shouldResetPinExpiryDate() {
            given(defendantPinToPostLRspecService.resetPinExpiryDate(any())).willReturn(pin);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().addRespondent1PinToPostLRspec(givenPin)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("respondent1PinToPostLRspec")
                .extracting("expiryDate")
                .isEqualTo(pin.getExpiryDate().toString());
            verify(defendantPinToPostLRspecService, times(1)).resetPinExpiryDate(givenPin);
        }

        @Test
        void shouldNotifyDefendantLip_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST1234")
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .claimDetailsNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now())
                .build();
            when(notificationsProperties.getRespondentDefendantResponseForSpec())
                .thenReturn("template-id");
            when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claim-continuing-online-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyDefendantLip_whenNoEmailIsEntered() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .partyEmail(null)
                                 .build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST1234")
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .build();
            // Given
            caseData.toBuilder().respondent1Represented(YesOrNo.NO).specRespondent1Represented(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            handler.handle(params);

            // Then
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }
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
}
