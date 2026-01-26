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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;

@ExtendWith(MockitoExtension.class)
class HWFFeePaymentOutcomeServiceTest {

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
            .caseDataLip(new CaseDataLiP()
                             .setHelpWithFees(new HelpWithFees()
                                                  .setHelpWithFee(YesOrNo.NO)))
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfNumberAvailable(YesOrNo.YES)
                                          .setHwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                          .setHwfFullRemissionGrantedForClaimIssue(YesOrNo.YES))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getCaseDataLiP().getHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo("HWF-1C4-E34");
        assertThat(caseData.getCaseDataLiP().getHelpWithFees().getHelpWithFee()).isEqualTo(YesOrNo.YES);
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isNull();
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).isNull();
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenHWFRefNumberProvidedForClaimIssueWithNullCaseDataLiP() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfNumberAvailable(YesOrNo.YES)
                                          .setHwfNumberForFeePaymentOutcome("HWF-2B5-F45")
                                          .setHwfFullRemissionGrantedForClaimIssue(YesOrNo.YES))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getCaseDataLiP()).isNotNull();
        assertThat(caseData.getCaseDataLiP().getHelpWithFees()).isNotNull();
        assertThat(caseData.getCaseDataLiP().getHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo("HWF-2B5-F45");
        assertThat(caseData.getCaseDataLiP().getHelpWithFees().getHelpWithFee()).isEqualTo(YesOrNo.YES);
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenHWFRefNumberProvidedForHearingFee() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfNumberAvailable(YesOrNo.YES)
                                          .setHwfNumberForFeePaymentOutcome("HWF-1C4-E34")
                                          .setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES))
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getHearingHelpFeesReferenceNumber()).isEqualTo("HWF-1C4-E34");
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isNull();
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).isNull();
    }

    @Test
    void whenHWFRefNumberNotAvailable_shouldNotUpdateReferenceNumber() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfNumberAvailable(YesOrNo.NO)
                                          .setHwfNumberForFeePaymentOutcome("HWF-1C4-E34"))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getCaseDataLiP()).isNull();
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenFeePaymentOutcomeDetailsIsNull_shouldReturnCaseDataUnchanged() {
        CaseData caseData = CaseData.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        CaseData result = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(result).isEqualTo(caseData);
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenHwfNumberAvailableIsNull_shouldNotUpdateReferenceNumber() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfNumberForFeePaymentOutcome("HWF-1C4-E34"))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getCaseDataLiP()).isNull();
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenBothClaimIssuedAndHearingTypesAreSet_shouldUpdateBoth() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfNumberAvailable(YesOrNo.YES)
                                          .setHwfNumberForFeePaymentOutcome("HWF-3C6-G56")
                                          .setHwfFullRemissionGrantedForClaimIssue(YesOrNo.YES)
                                          .setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getCaseDataLiP()).isNotNull();
        assertThat(caseData.getCaseDataLiP().getHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo("HWF-3C6-G56");
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountForClaimIssue() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
            .claimIssuedHwfDetails(new HelpWithFeesDetails()
                                       .setRemissionAmount(BigDecimal.valueOf(1000)))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(90).setScale(
            2));
        assertThat(caseData.getClaimIssuedHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(1000));
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountForClaimIssueWithNullDetails() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getClaimIssuedHwfDetails()).isNotNull();
        assertThat(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(100).setScale(
            2));
        assertThat(caseData.getClaimIssuedHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.ZERO);
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountFoeHearingFee() {
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hearingHwfDetails(new HelpWithFeesDetails()
                                   .setRemissionAmount(BigDecimal.valueOf(1000)))
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getHearingHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(290).setScale(
            2));
        assertThat(caseData.getHearingHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.valueOf(1000));
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenItsPartRemissionUpdateOutstandingAmountForHearingFeeWithNullDetails() {
        CaseData caseData = CaseData.builder()
            .hearingReferenceNumber("000HN001")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getHearingHwfDetails()).isNotNull();
        assertThat(caseData.getHearingHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(300).setScale(
            2));
        assertThat(caseData.getHearingHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.ZERO);
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenNoRemissionHwfForClaimIssue_shouldSetRemissionToZero() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).code("OOOCM002").build())
            .claimIssuedHwfDetails(new HelpWithFeesDetails()
                                       .setRemissionAmount(BigDecimal.valueOf(5000)))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, NO_REMISSION_HWF.name());
        assertThat(caseData.getClaimIssuedHwfDetails()).isNotNull();
        assertThat(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(100).setScale(
            2));
        assertThat(caseData.getClaimIssuedHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.ZERO);
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenNoRemissionHwfForHearingFee_shouldSetRemissionToZero() {
        CaseData caseData = CaseData.builder()
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hearingHwfDetails(new HelpWithFeesDetails()
                                   .setRemissionAmount(BigDecimal.valueOf(10000)))
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, NO_REMISSION_HWF.name());
        assertThat(caseData.getHearingHwfDetails()).isNotNull();
        assertThat(caseData.getHearingHwfDetails().getOutstandingFeeInPounds()).isEqualTo(BigDecimal.valueOf(300).setScale(
            2));
        assertThat(caseData.getHearingHwfDetails().getRemissionAmount()).isEqualTo(BigDecimal.ZERO);
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenClaimFeeIsZero_shouldNotUpdateOutstandingAmount() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.ZERO).build())
            .claimIssuedHwfDetails(new HelpWithFeesDetails()
                                       .setRemissionAmount(BigDecimal.valueOf(1000)))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getClaimIssuedHwfDetails()).isNotNull();
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenHearingFeeIsZero_shouldNotUpdateOutstandingAmount() {
        CaseData caseData = CaseData.builder()
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.ZERO).build())
            .hearingHwfDetails(new HelpWithFeesDetails()
                                   .setRemissionAmount(BigDecimal.valueOf(1000)))
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getHearingHwfDetails()).isNotNull();
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenClaimFeeIsNull_shouldNotUpdateOutstandingAmount() {
        CaseData caseData = CaseData.builder()
            .claimIssuedHwfDetails(new HelpWithFeesDetails()
                                       .setRemissionAmount(BigDecimal.valueOf(1000)))
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenHearingFeeIsNull_shouldNotUpdateOutstandingAmount() {
        CaseData caseData = CaseData.builder()
            .hearingHwfDetails(new HelpWithFeesDetails()
                                   .setRemissionAmount(BigDecimal.valueOf(1000)))
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenNeitherClaimIssuedNorHearingTypeIsSet_shouldNotUpdateAnything() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).build())
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getClaimIssuedHwfDetails()).isNull();
        assertThat(caseData.getHearingHwfDetails()).isNull();
        verifyNoInteractions(helpWithFeesForTabService);
    }

    @Test
    void whenRemissionAmountIsNull_shouldHandleGracefullyForClaimIssue() {
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(10000)).build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getClaimIssuedHwfDetails()).isNotNull();
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenRemissionAmountIsNull_shouldHandleGracefullyForHearingFee() {
        CaseData caseData = CaseData.builder()
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateOutstandingFee(caseData, PARTIAL_REMISSION_HWF_GRANTED.name());
        assertThat(caseData.getHearingHwfDetails()).isNotNull();
        verify(helpWithFeesForTabService).setUpHelpWithFeeTab(any(CaseData.class));
    }

    @Test
    void whenHWFRefNumberProvidedForBothClaimAndHearing_shouldClearPropertiesForBoth() {
        CaseData caseData = CaseData.builder()
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails().setHwfNumberAvailable(YesOrNo.YES)
                                          .setHwfNumberForFeePaymentOutcome("HWF-4D7-H67")
                                          .setHwfFullRemissionGrantedForClaimIssue(YesOrNo.YES)
                                          .setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES))
            .hwfFeeType(FeeType.HEARING)
            .build();

        caseData = feePaymentOutcomeService.updateHwfReferenceNumber(caseData);
        assertThat(caseData.getHearingHelpFeesReferenceNumber()).isEqualTo("HWF-4D7-H67");
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable()).isNull();
        assertThat(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome()).isNull();
    }

}
