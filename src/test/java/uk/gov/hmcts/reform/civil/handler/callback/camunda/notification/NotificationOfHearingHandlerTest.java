package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.hearing.HearingNotificationEmailConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.fees.client.FeesClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationOfHearingHandler.TASK_ID;

@SpringBootTest(classes = {
    NotificationOfHearingHandler.class,
    JacksonAutoConfiguration.class,
    InterestCalculator.class,
    FeesService.class,
    FeesClient.class
})
public class NotificationOfHearingHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @Autowired
    private NotificationOfHearingHandler handler;
    @MockBean
    private InterestCalculator interestCalculator;
    @MockBean
    private FeesService feesService;
    @MockBean
    private HearingNotificationEmailConfiguration hearingNotificationEmailConfiguration;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotificationOfHearing())
                .thenReturn("test-template-received-id");
            when(hearingNotificationEmailConfiguration.getReceiver())
                .thenReturn("caseworker@hmcts.net");

        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAnd1v1() {
            //send Received email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "caseworker@hmcts.net",
                "test-template-received-id",
                getNotificationDataMap(caseData),
                "default-judgment-caseworker-received-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedPartialPaymentAnd1v1() {
            when(interestCalculator.calculateInterest(any()))
                .thenReturn(BigDecimal.valueOf(100)
                );
            when(feesService.getFeeDataByTotalClaimAmount(any()))
                .thenReturn(Fee.builder()
                                .calculatedAmountInPence(BigDecimal.valueOf(100))
                                .version("1")
                                .code("CODE")
                                .build());
            //send Received email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YesOrNo.NO)
                .totalClaimAmount(new BigDecimal(1000))
                .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
                .repaymentSuggestion("10000")
                .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "caseworker@hmcts.net",
                "test-template-received-id",
                getNotificationDataMapPartialPayment(caseData),
                "default-judgment-caseworker-received-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_NUMBER, caseData.getLegacyCaseReference()
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapPartialPayment(CaseData caseData) {
            return Map.of(
                CLAIM_NUMBER, caseData.getLegacyCaseReference()
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_CLAIMANT_HEARING").build()).build())).isEqualTo(TASK_ID);
    }
}
