package uk.gov.hmcts.reform.civil.service.citizenui;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesForTab;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

@ExtendWith(MockitoExtension.class)
public class HelpWithFeesForTabServiceTest {

    private HelpWithFeesForTabService helpWithFeesForTabService;

    @BeforeEach
    public void setUp() {
        helpWithFeesForTabService = new HelpWithFeesForTabService();
    }

    @Test
    public void shouldSetUpClaimIssueHelpWithFeeTab() {
        // Given
        CaseData.CaseDataBuilder builder = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
               .remissionAmount(new BigDecimal("50000"))
               .outstandingFeeInPounds(new BigDecimal("30"))
               .build())
            .caseDataLip(CaseDataLiP.builder()
                .helpWithFees(HelpWithFees.builder()
                .helpWithFeesReferenceNumber("HWF-REF")
                .build()).build())
            .claimFee(Fee.builder()
                .calculatedAmountInPence(new BigDecimal("1000"))
                .code("CODE").build())
            .build().toBuilder();

        // When
        helpWithFeesForTabService.setUpHelpWithFeeTab(builder);

        // Then
        CaseData caseData = builder.build();
        HelpWithFeesForTab hwfForTab = caseData.getClaimIssuedHwfForTab();
        assertThat(hwfForTab.getClaimFee()).isEqualTo("10.00");
        assertThat(hwfForTab.getFeeCode()).isEqualTo("CODE");
        assertThat(hwfForTab.getHwfType()).isEqualTo("Claim Fee");
        assertThat(hwfForTab.getHwfReferenceNumber()).isEqualTo("HWF-REF");
        assertThat(hwfForTab.getRemissionAmount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(hwfForTab.getApplicantMustPay()).isEqualTo(new BigDecimal("30"));
    }

    @Test
    public void shouldSetUpHearingHelpWithFeeTab() {
        // Given
        CaseData.CaseDataBuilder builder = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
               .remissionAmount(new BigDecimal("50000"))
               .outstandingFeeInPounds(new BigDecimal("30"))
               .build())
            .caseDataLip(CaseDataLiP.builder()
                .helpWithFees(HelpWithFees.builder()
                .helpWithFeesReferenceNumber("HWF-REF")
                .build()).build())
            .hearingFee(Fee.builder()
                .calculatedAmountInPence(new BigDecimal("1000"))
                .code("CODE").build())
            .build().toBuilder()
            .hearingHelpFeesReferenceNumber("HWF-REF");

        // When
        helpWithFeesForTabService.setUpHelpWithFeeTab(builder);

        // Then
        CaseData caseData = builder.build();
        HelpWithFeesForTab hwfForTab = caseData.getHearingHwfForTab();
        assertThat(hwfForTab.getClaimFee()).isEqualTo("10.00");
        assertThat(hwfForTab.getFeeCode()).isEqualTo("CODE");
        assertThat(hwfForTab.getHwfType()).isEqualTo("Hearing Fee");
        assertThat(hwfForTab.getHwfReferenceNumber()).isEqualTo("HWF-REF");
        assertThat(hwfForTab.getRemissionAmount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(hwfForTab.getApplicantMustPay()).isEqualTo(new BigDecimal("30"));
    }
}
