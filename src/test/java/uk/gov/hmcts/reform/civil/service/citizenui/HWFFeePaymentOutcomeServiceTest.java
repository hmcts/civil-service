package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HWFFeePaymentOutcomeServiceTest {

    private HWFFeePaymentOutcomeService feePaymentOutcomeService;

    @BeforeEach
    void setUp() {
        feePaymentOutcomeService = new HWFFeePaymentOutcomeService();
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountForClaimIssue() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .remissionAmount(BigDecimal.valueOf(1000))
                                       .build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData);
        assertThat(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(90).setScale(
            2));
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountForHearingFee() {
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .remissionAmount(BigDecimal.valueOf(1000))
                                   .build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData);
        assertThat(caseData.getHearingHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(290).setScale(
            2));
    }
}
