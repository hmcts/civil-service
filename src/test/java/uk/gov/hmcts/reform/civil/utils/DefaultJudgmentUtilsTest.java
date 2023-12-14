package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils.calculateFixedCosts;

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
}
