package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HearingFeeHelperTest {

    private HearingFeeHelper helper;

    @BeforeEach
    void setUp() {
        helper = new HearingFeeHelper();
    }

    @Nested
    class IsHearingFeePaid {

        @Test
        void shouldReturnTrue_whenSuccessfulPaymentBeforeDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().minusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenPaymentDoneWithHWF() {
            CaseData caseData = CaseData.builder()
                .hearingHelpFeesReferenceNumber("REF123")
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                              .setHwfFullRemissionGrantedForHearingFee(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES))
                .build();

            assertThat(helper.isHearingFeePaid(null, caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenSuccessfulPaymentOnDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now())
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenSuccessfulPaymentAfterDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().plusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenFailedPaymentAndNoHWF() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().minusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenNullPaymentDetailsAndNoHWF() {
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().minusDays(1))
                .build();

            assertThat(helper.isHearingFeePaid(null, caseData)).isFalse();
        }
    }

    @Nested
    class IsHearingFeeUnpaid {

        @Test
        void shouldReturnTrue_whenNullPaymentDetailsAndAfterDueDate() {
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().minusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(null, caseData)).isTrue();
        }

        @Test
        void shouldReturnTrue_whenFailedPaymentAndAfterDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().minusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(paymentDetails, caseData)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenSuccessfulPaymentAndAfterDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.SUCCESS);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().minusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(paymentDetails, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenNullPaymentDetailsButBeforeDueDate() {
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().plusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(null, caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenFailedPaymentButBeforeDueDate() {
            PaymentDetails paymentDetails = new PaymentDetails().setStatus(PaymentStatus.FAILED);
            CaseData caseData = CaseData.builder()
                .hearingDueDate(LocalDate.now().plusDays(1))
                .build();

            assertThat(helper.isHearingFeeUnpaid(paymentDetails, caseData)).isFalse();
        }
    }
}
