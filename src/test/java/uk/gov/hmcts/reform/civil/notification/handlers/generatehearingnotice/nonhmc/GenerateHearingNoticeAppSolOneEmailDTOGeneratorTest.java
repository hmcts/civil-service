package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeAppSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_REF = "notification-of-hearing-%s";
    private static final String NO_FEE_TEMPLATE = "noFeeTemplate";
    private static final String NO_FEE_OTHER_TEMPLATE = "noFeeOtherTemplate";
    private static final String NO_FEE_RELISTING_TEMPLATE = "noFeeRelistingTemplate";
    private static final String FEE_TEMPLATE = "feeTemplate";

    private static final String TIME_1500 = "1500";
    private static final String TIME_0800 = "0800";
    private static final String TIME_1015 = "1015";
    private static final String TIME_1115 = "1115";
    private static final String TIME_0900 = "0900";

    private static final LocalDate DATE_2025_06_30 = LocalDate.of(2025, 6, 30);
    private static final LocalDate DATE_2025_07_04 = LocalDate.of(2025, 7, 4);
    private static final LocalDate DATE_2025_07_01 = LocalDate.of(2025, 7, 1);
    private static final LocalDate DATE_2025_08_15 = LocalDate.of(2025, 8, 15);

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private GenerateHearingNoticeAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnNoFeeTemplate_whenPaymentSuccess() {
        CaseData caseData = baseBuilder()
                .hearingFeePaymentDetails(new PaymentDetails().setStatus(SUCCESS))
                .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();

        when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn(NO_FEE_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(NO_FEE_TEMPLATE, actual);
    }

    @Test
    void shouldReturnNoFeeTemplate_whenOtherList() {
        CaseData caseData = baseBuilder()
                .hearingFeePaymentDetails(null)
                .hearingNoticeList(HearingNoticeList.OTHER)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();

        when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn(NO_FEE_OTHER_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(NO_FEE_OTHER_TEMPLATE, actual);
    }

    @Test
    void shouldReturnNoFeeTemplate_whenRelisting() {
        CaseData caseData = baseBuilder()
                .hearingFeePaymentDetails(null)
                .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
                .listingOrRelisting(ListingOrRelisting.RELISTING)
                .build();

        when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplate())
                .thenReturn(NO_FEE_RELISTING_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(NO_FEE_RELISTING_TEMPLATE, actual);
    }

    @Test
    void shouldReturnFeeTemplate_whenFeeRequired() {
        CaseData caseData = baseBuilder()
                .hearingFeePaymentDetails(null)
                .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .build();

        when(notificationsProperties.getHearingListedFeeClaimantLrTemplate())
                .thenReturn(FEE_TEMPLATE);

        String actual = generator.getEmailTemplateId(caseData);

        assertEquals(FEE_TEMPLATE, actual);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String actual = generator.getReferenceTemplate();
        assertEquals(TEMPLATE_REF, actual);
    }

    @Test
    void shouldAddZeroFee_whenFeeNull() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_1015))
                    .thenReturn("10:15am");
            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_04))
                    .thenReturn("04-07-2025");
            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_01))
                .thenReturn("01-07-2025");

            CaseData caseData = baseBuilder()
                    .hearingTimeHourMinute(TIME_1015)
                    .hearingFeePaymentDetails(null)
                    .hearingFee(null)
                    .hearingDueDate(DATE_2025_07_04)
                    .hearingDate(DATE_2025_07_01)
                    .build();

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("zero-fee-branch",
                    () -> assertEquals("10:15am", result.get(HEARING_TIME)),
                    () -> assertEquals("£0.00", result.get(HEARING_FEE)),
                    () -> assertEquals("04-07-2025", result.get(HEARING_DUE_DATE)),
                    () -> assertEquals("01-07-2025", result.get(HEARING_DATE))
            );
        }
    }

    @Test
    void shouldAddEmptyDueDate_whenDueDateNull() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_1115))
                    .thenReturn("11:15am");

            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_04))
                .thenReturn("04-07-2025");

            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_01))
                .thenReturn("01-07-2025");

            CaseData caseData = baseBuilder()
                    .hearingTimeHourMinute(TIME_1115)
                    .hearingFeePaymentDetails(null)
                    .hearingFee(new Fee().setCalculatedAmountInPence(valueOf(25000)))
                    .hearingDueDate(null)
                    .hearingDate(DATE_2025_07_01)
                    .build();

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("empty-due-date-branch",
                    () -> assertEquals("11:15am", result.get(HEARING_TIME)),
                    () -> assertEquals("£250.00", result.get(HEARING_FEE)),
                    () -> assertEquals("", result.get(HEARING_DUE_DATE)),
                    () -> assertEquals("01-07-2025", result.get(HEARING_DATE))
            );
        }
    }

    @Test
    void shouldAddDateTimeFeeAndDueDate_whenNoSuccessfulPayment() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_1500))
                    .thenReturn("3:00pm");
            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_06_30))
                    .thenReturn("30-06-2025");

            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_01))
                .thenReturn("01-07-2025");

            CaseData caseData = baseBuilder()
                    .hearingTimeHourMinute(TIME_1500)
                    .hearingFeePaymentDetails(null)
                    .hearingFee(new Fee().setCalculatedAmountInPence(valueOf(20000)))
                    .hearingDueDate(DATE_2025_06_30)
                    .hearingDate(DATE_2025_07_01)
                    .build();

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("no-successful-payment-branch",
                    () -> assertEquals("3:00pm", result.get(HEARING_TIME)),
                    () -> assertEquals("£200.00", result.get(HEARING_FEE)),
                    () -> assertEquals("30-06-2025", result.get(HEARING_DUE_DATE)),
                      () -> assertEquals("01-07-2025", result.get(HEARING_DATE))
            );
        }
    }

    @Test
    void shouldNotAddFeeAndDueDate_whenPaymentSuccessful() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_0800))
                    .thenReturn("formattedTime");

            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_01))
                .thenReturn("01-07-2025");

            CaseData caseData = baseBuilder()
                    .hearingTimeHourMinute(TIME_0800)
                    .hearingFeePaymentDetails(new PaymentDetails().setStatus(SUCCESS))
                    .hearingFee(new Fee().setCalculatedAmountInPence(valueOf(10000)))
                    .hearingDueDate(DATE_2025_07_01)
                    .hearingDate(DATE_2025_07_01)
                    .build();

            utils.when(() -> getApplicantLegalOrganizationName(eq(caseData), any()))
                .thenReturn("Applicant Org Ltd");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("payment-successful-branch",
                    () -> assertEquals("01-07-2025", result.get(HEARING_DATE)),
                    () -> assertEquals("formattedTime", result.get(HEARING_TIME)),
                    () -> assertFalse(result.containsKey(HEARING_FEE)),
                    () -> assertFalse(result.containsKey(HEARING_DUE_DATE)),
                    () -> assertEquals("Applicant Org Ltd", result.get(CLAIM_LEGAL_ORG_NAME_SPEC))
            );
        }
    }

    @Test
    void shouldAddFeeAndDueDate_whenPaymentFailed() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getFormattedHearingTime(TIME_0900))
                    .thenReturn("9:00am");
            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_08_15))
                    .thenReturn("15-08-2025");

            utils.when(() -> NotificationUtils.getFormattedHearingDate(DATE_2025_07_01))
                .thenReturn("01-07-2025");

            CaseData caseData = baseBuilder()
                    .hearingTimeHourMinute(TIME_0900)
                    .hearingFeePaymentDetails(new PaymentDetails().setStatus(FAILED))
                    .hearingFee(new Fee().setCalculatedAmountInPence(valueOf(15000)))
                    .hearingDueDate(DATE_2025_08_15)
                    .hearingDate(DATE_2025_07_01)
                    .build();

            utils.when(() -> getApplicantLegalOrganizationName(eq(caseData), any()))
                .thenReturn("Applicant Org Ltd");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("payment-failed-branch",
                    () -> assertEquals("01-07-2025", result.get(HEARING_DATE)),
                    () -> assertEquals("9:00am", result.get(HEARING_TIME)),
                    () -> assertEquals("£150.00", result.get(HEARING_FEE)),
                    () -> assertEquals("15-08-2025", result.get(HEARING_DUE_DATE)),
                      () -> assertEquals("Applicant Org Ltd", result.get(CLAIM_LEGAL_ORG_NAME_SPEC))
            );
        }
    }

    private static CaseData.CaseDataBuilder<?, ?> baseBuilder() {
        return CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build()
                .toBuilder();
    }
}
