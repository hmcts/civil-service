package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PAYMENT_TYPE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class DJCaseworkerReceivedNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DJCaseworkerReceivedNotificationHandler handler;

    @Mock
    private InterestCalculator interestCalculator;

    @Mock
    private FeesService feesService;

    @Mock
    private DefaultJudgmentSpecEmailConfiguration defaultJudgmentSpecEmailConfiguration;

    @Mock
    private ObjectMapper objectMapper;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedAnd1v1() {
            when(notificationsProperties.getCaseworkerDefaultJudgmentRequested())
                .thenReturn("test-template-received-id");
            when(defaultJudgmentSpecEmailConfiguration.getReceiver())
                .thenReturn("caseworker@hmcts.net");
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
            when(notificationsProperties.getCaseworkerDefaultJudgmentRequested())
                .thenReturn("test-template-received-id");
            when(defaultJudgmentSpecEmailConfiguration.getReceiver())
                .thenReturn("caseworker@hmcts.net");
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
                CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
                PAYMENT_TYPE, "Immediately £1203.00",
                AMOUNT_CLAIMED, "1100",
                RESPONDENT, "John Doe",
                AMOUNT_OF_COSTS, "103.00",
                AMOUNT_PAID, "0",
                AMOUNT_OF_JUDGMENT, "1203.00",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapPartialPayment(CaseData caseData) {
            return Map.of(
                CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
                PAYMENT_TYPE, "By installments of £100.00 per two weeks",
                AMOUNT_CLAIMED, "1100",
                RESPONDENT, "John Doe",
                AMOUNT_OF_COSTS, "103.00",
                AMOUNT_PAID, "0",
                AMOUNT_OF_JUDGMENT, "1203.00",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234",
                CASEMAN_REF, "000DC001"
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
