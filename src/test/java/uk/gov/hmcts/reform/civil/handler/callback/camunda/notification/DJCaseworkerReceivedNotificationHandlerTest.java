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
import uk.gov.hmcts.reform.civil.config.properties.defaultjudgments.DefaultJudgmentSpecEmailConfiguration;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.fees.client.FeesApi;
import uk.gov.hmcts.reform.fees.client.FeesClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DJCaseworkerReceivedNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT_OF_COSTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PAYMENT_TYPE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT;

@SpringBootTest(classes = {
    DJCaseworkerReceivedNotificationHandler.class,
    JacksonAutoConfiguration.class,
    InterestCalculator.class,
    FeesService.class,
    FeesClient.class
})
public class DJCaseworkerReceivedNotificationHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @Autowired
    private DJCaseworkerReceivedNotificationHandler handler;
    @MockBean
    private InterestCalculator interestCalculator;
    @MockBean
    private FeesService feesService;
    @MockBean
    private DefaultJudgmentSpecEmailConfiguration defaultJudgmentSpecEmailConfiguration;
    @MockBean
    private FeesApi feesApi;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getCaseworkerDefaultJudgmentRequested())
                .thenReturn("test-template-received-id");
            when(defaultJudgmentSpecEmailConfiguration.getReceiver())
                .thenReturn("caseworker@hmcts.net");

        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAnd1v1() {
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build().toBuilder()
                .totalClaimAmount(new BigDecimal(1000))
                .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build().toBuilder()
                .addRespondent2(YesOrNo.NO)
                .totalClaimAmount(new BigDecimal(1000))
                .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
                .repaymentSuggestion("10000")
                .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
                .paymentConfirmationDecisionSpec(YesOrNo.YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
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

        @Test
        void shouldNotNotifyApplicantSolicitor_whenJudgmentIsGranted() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .addRespondent2(YesOrNo.NO).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_NUMBER, caseData.getLegacyCaseReference(),
                PAYMENT_TYPE, "Immediately £1203.00",
                AMOUNT_CLAIMED, "1100",
                RESPONDENT, "John Doe",
                AMOUNT_OF_COSTS, "103.00",
                AMOUNT_PAID, "0",
                AMOUNT_OF_JUDGMENT, "1203.00"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapPartialPayment(CaseData caseData) {
            return Map.of(
                CLAIM_NUMBER, caseData.getLegacyCaseReference(),
                PAYMENT_TYPE, "By installments of £100.00 per two weeks",
                AMOUNT_CLAIMED, "1100",
                RESPONDENT, "John Doe",
                AMOUNT_OF_COSTS, "103.00",
                AMOUNT_PAID, "0",
                AMOUNT_OF_JUDGMENT, "1203.00"
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
            "NOTIFY_CASEWORKER_DJ_RECEIVED").build()).build())).isEqualTo(TASK_ID);
    }
}
