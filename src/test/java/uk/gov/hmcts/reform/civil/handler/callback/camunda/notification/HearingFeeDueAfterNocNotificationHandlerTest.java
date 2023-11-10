package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.HearingFeeDueAfterNocNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    HearingFeeDueAfterNocNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
class HearingFeeDueAfterNocNotificationHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private HearingFeeDueAfterNocNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSendClaimantEmail_whenInvoked() {
            when(notificationsProperties.getHearingFeeUnpaidNoc()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test org name").build()));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build().toBuilder()
                .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
                .hearingDate(LocalDate.of(1990, 2, 20))
                .hearingTimeHourMinute("1215")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(1990, 2, 20))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap1(caseData),
                "NOC-hearing-fee-unpaid-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldSendClaimantEmail_whenInvokedNoOrganisationName() {
            when(notificationsProperties.getHearingFeeUnpaidNoc()).thenReturn(TEMPLATE_ID);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build().toBuilder()
                .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
                .hearingDate(LocalDate.of(1990, 2, 20))
                .hearingTimeHourMinute("1215")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(1990, 2, 20))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap2(caseData),
                "NOC-hearing-fee-unpaid-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotSendClaimantEmail_whenInvokedAndFeeAlreadyPaid() {
            when(notificationsProperties.getHearingFeeUnpaidNoc()).thenReturn(TEMPLATE_ID);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build().toBuilder()
                .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
                .hearingDate(LocalDate.of(1990, 2, 20))
                .hearingTimeHourMinute("1215")
                .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
                .hearingDueDate(LocalDate.of(1990, 2, 20))
                .hearingFeePaymentDetails(PaymentDetails.builder()
                                              .status(SUCCESS)
                                              .reference("REFERENCE")
                                              .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap2(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            LEGAL_ORG_NAME, "Signer Name",
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, "County Court",
            HEARING_TIME, "1215",
            HEARING_FEE, "£300.00",
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE)
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap1(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            LEGAL_ORG_NAME, "Test org name",
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, "County Court",
            HEARING_TIME, "1215",
            HEARING_FEE, "£300.00",
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE)
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "HearingFeeDueNotifyApplicantSolicitorAfterNoc").build()).build())).isEqualTo(TASK_ID);
    }

    @Test
    void shouldOnlyContainEvent_whenInvoked() {
        assertThat(handler.handledEvents()).containsOnly(NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC);
    }

}
