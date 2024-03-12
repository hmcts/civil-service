package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;

@ExtendWith(MockitoExtension.class)
public class HWFFeePaymentOutcomeServiceTest {

    private HWFFeePaymentOutcomeService feePaymentOutcomeService;
    @Mock
    private HelpWithFeesForTabService helpWithFeesForTabService;

    @BeforeEach
    void setUp() {
        feePaymentOutcomeService = new HWFFeePaymentOutcomeService(helpWithFeesForTabService);
    }

    @Test
    void whenHWFRefNumberProvidedForClaimIssue() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder()
                             .helpWithFees(HelpWithFees.builder()
                                               .helpWithFee(YesOrNo.NO).build()).build())
            .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                          .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                          .hwfFullRemissionGrantedForClaimIssue(YesOrNo.YES).build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getCaseDataLiP().getHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo("HWF-1C4-E34");
    }

    @Test
    void whenHWFRefNumberProvidedForHearingFee() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder().hwfNumberAvailable(YesOrNo.YES)
                                          .hwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                          .hwfFullRemissionGrantedForHearingFee(YesOrNo.YES).build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getHearingHelpFeesReferenceNumber()).isEqualTo("HWF-1C4-E34");
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).isNull();
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

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(90).setScale(2));
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountFoeHearingFee() {
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .remissionAmount(BigDecimal.valueOf(1000))
                                   .build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getHearingHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(290).setScale(2));

    }

}
