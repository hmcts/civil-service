package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils.calculateFixedCosts;
import static uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils.calculateFixedCostsOnEntry;

public class DefaultJudgmentUtilsTest {

    @Test
    void shouldReturnFixedCost_whenClaimAmountIsInZeroInstalment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(10)).build();
        BigDecimal result = calculateFixedCosts(caseData);
        assertThat(result).isEqualTo(new BigDecimal(0));
    }

    @Test
    void shouldReturnFixedCost_whenClaimAmountIsInFirstInstalment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(30)).build();
        BigDecimal result = calculateFixedCosts(caseData);
        assertThat(result).isEqualTo(new BigDecimal(82));
    }

    @Test
    void shouldReturnFixedCost_whenClaimAmountIsInSecondInstalment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(560)).build();
        BigDecimal result = calculateFixedCosts(caseData);
        assertThat(result).isEqualTo(new BigDecimal(102));
    }

    @Test
    void shouldReturnFixedCost_whenClaimAmountIsInThirdInstalment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(2000)).build();
        BigDecimal result = calculateFixedCosts(caseData);
        assertThat(result).isEqualTo(new BigDecimal(112));
    }

    @Test
    void shouldReturnFixedCost_whenClaimAmountIsInFourthInstalment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(8000)).build();
        BigDecimal result = calculateFixedCosts(caseData);
        assertThat(result).isEqualTo(new BigDecimal(140));
    }

    @Test
    void shouldReturnFixedCostOnEntry_whenJudgmentAmountIsMoreThan5000() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
            .fixedCosts(FixedCosts.builder()
                            .fixedCostAmount("10000")
                            .build()).build();
        BigDecimal result = calculateFixedCostsOnEntry(caseData, new BigDecimal(5001));
        assertThat(result).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
            Integer.parseInt("13000"))));
    }

    @Test
    void shouldReturnFixedCostOnEntry_whenJudgmentAmountIsMoreThan25LessThan5000() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
            .fixedCosts(FixedCosts.builder()
                            .fixedCostAmount("10000")
                            .build()).build();
        BigDecimal result = calculateFixedCostsOnEntry(caseData, new BigDecimal(5000));
        assertThat(result).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
            Integer.parseInt("12200"))));
    }

    @Test
    void shouldReturnFixedCostOnEntry_whenJudgmentAmountIsUpto25() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .addRespondent2(YesOrNo.NO)
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
            .fixedCosts(FixedCosts.builder()
                            .fixedCostAmount("10000")
                            .build()).build();
        BigDecimal result = calculateFixedCostsOnEntry(caseData, new BigDecimal(25));
        assertThat(result).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
            Integer.parseInt("10000"))));
    }
}
