package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class HearingFeeHelperTest {

    private HearingFeeHelper helper;

    @Mock
    private Time time;

    private static final LocalDateTime NOW = LocalDateTime.of(2023, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        lenient().when(time.now()).thenReturn(NOW);
        helper = new HearingFeeHelper(time);
    }

    @Nested
    class IsHearingFeePaid {

        @Test
        void shouldReturnTrue_whenSuccessfulPaymentBeforeDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().minusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenPaymentDoneWithHWF() {
            CaseData caseData = CaseDataBuilder.builder()
                .hearingHelpFeesReferenceNumber("REF123")
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                              .setHwfFullRemissionGrantedForHearingFee(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES))
                .build();

            assertThat(helper.isHearingFeePaid(null, caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenSuccessfulPaymentOnDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate())
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenSuccessfulPaymentAfterDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().plusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenFailedPaymentAndNoHWF() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().minusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenNullPaymentDetailsAndNoHWF() {
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().minusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(null, caseData)).isFalse();
        }
    }

    @Nested
    class IsHearingFeeUnpaid {

        @Test
        void shouldReturnTrue_whenNullPaymentDetailsAndAfterDueDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().minusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(null, caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenFailedPaymentAndAfterDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().minusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(paymentDetails, caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenSuccessfulPaymentAndAfterDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().minusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenNullPaymentDetailsButBeforeDueDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().plusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(null, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenFailedPaymentButBeforeDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseDataBuilder.builder()
                .hearingDueDate(NOW.toLocalDate().plusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(paymentDetails, caseData)).isFalse();
        }
    }
}
