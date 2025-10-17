package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.getPartialPayment;

@ExtendWith(MockitoExtension.class)
class JudgmentAmountsCalculatorTest {

    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    private static final GaCaseDataEnricher GA_CASE_DATA_ENRICHER = new GaCaseDataEnricher();

    @Mock
    private InterestCalculator interestCalculator;

    @InjectMocks
    private JudgmentAmountsCalculator judgmentAmountsCalculator;

    @Test
    void shouldGetPartialPaymentInPoundsFromCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .partialPaymentAmount("15000")
            .build();

        assertThat(getPartialPayment(caseData)).isEqualTo("150.00");
    }

    @Test
    void shouldReturnClaimFeeWithOutstandingFee_whenHelpWithFeesIsTrue() {

        CaseData caseData = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(1000)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().outstandingFeeInPounds(BigDecimal.valueOf(50)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .totalClaimAmount(new BigDecimal(2000))
            .caseDataLip(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).build()).build())
            .build();

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("50.00");
    }

    @Test
    void shouldReturnClaimFeeWithFixedCosts_whenFixedCostsAreProvided() {
        CaseData caseData = gaCaseData(builder -> builder
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .fixedCosts(FixedCosts.builder()
                .fixedCostAmount("1000")
                .claimFixedCosts(YesOrNo.YES)
                .build())
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal("1000")).build()));

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("20.00");
    }

    @Test
    void shouldReturnClaimFeeWithCalculatedFixedCostsOnDJEntry_whenFixedCostsAreProvided() {
        CaseData caseData = gaCaseData(builder -> builder
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .totalClaimAmount(new BigDecimal("5000"))
            .fixedCosts(FixedCosts.builder()
                .fixedCostAmount("1000")
                .claimFixedCosts(YesOrNo.YES)
                .build())
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal("9000")).build()));

        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("50.00"));

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("130.00");
    }

    @Test
    void shouldReturnClaimFeeWithFixedCosts_whenFixedCostsAreProvidedAndPaymentConfirmationDecisionNo() {
        CaseData caseData = gaCaseData(builder -> builder
            .paymentConfirmationDecisionSpec(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal("5000"))
            .fixedCosts(FixedCosts.builder()
                .fixedCostAmount("1000")
                .claimFixedCosts(YesOrNo.YES)
                .build())
            .claimFixedCostsOnEntryDJ(YesOrNo.NO)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal("8000")).build()));

        BigDecimal claimFee = judgmentAmountsCalculator.getClaimFee(caseData);

        assertThat(claimFee).isEqualTo("90.00");
    }

    @Test
    void shouldReturnDebtAmountWithInterest_whenInterestIsCalculated() {
        CaseData caseData = gaCaseData(builder -> builder
            .totalClaimAmount(new BigDecimal("1000")));
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("50.00"));

        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData);

        assertThat(debtAmount).isEqualTo("1050.00");
    }

    @Test
    void shouldReturnDebtAmountWithPartialPaymentDeducted_whenPartialPaymentIsProvided() {
        CaseData caseData = gaCaseData(builder -> builder
            .totalClaimAmount(new BigDecimal("1000"))
            .partialPaymentAmount("20000"));
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(new BigDecimal("50.00"));

        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData);

        assertThat(debtAmount).isEqualTo("850.00");
    }

    @Test
    void shouldReturnZeroDebtAmount_whenTotalClaimAmountAndInterestAreZero() {
        CaseData caseData = gaCaseData(builder -> builder
            .totalClaimAmount(BigDecimal.ZERO));
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);

        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData);

        assertThat(debtAmount).isEqualTo(BigDecimal.ZERO);
    }

    private CaseData gaCaseData(UnaryOperator<CaseData.CaseDataBuilder<?, ?>> customiser) {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withLocationName("Nottingham County Court and Family Court (and Crown)")
            .withGaCaseManagementLocation(GACaseLocation.builder()
                                              .siteName("testing")
                                              .address("london court")
                                              .baseLocation("000000")
                                              .postcode("BA 117")
                                              .build())
            .build();

        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = GA_CASE_DATA_ENRICHER.enrich(converted, gaCaseData);

        return customiser.apply(enriched.toBuilder()).build();
    }
}
