package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void shouldReturnOneDefendant_whenRespondent1HasNotRespondedToClaimIn1v2() {
        CaseData caseData = CaseDataBuilder.builder().atState1v2AndRespondentResponseDeadlinePassed().build().toBuilder()
            .respondent2ResponseDate(LocalDateTime.now()).build();
        List<String> expected = List.of("Mr. Sole Trader");
        List<String> actual = DefaultJudgmentUtils.getDefendants(caseData);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnOneDefendant_whenRespondent2HasNotRespondedToClaimIn1v2() {
        CaseData caseData = CaseDataBuilder.builder().atState1v2AndRespondentResponseDeadlinePassed().build().toBuilder()
            .respondent1ResponseDate(LocalDateTime.now()).build();
        List<String> expected = List.of("Mr. John Rambo");
        List<String> actual = DefaultJudgmentUtils.getDefendants(caseData);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnBothDefendants_whenBothRespondentsHaveNotRespondedToClaimIn1v2() {
        CaseData caseData = CaseDataBuilder.builder().atState1v2AndRespondentResponseDeadlinePassed().build();
        List<String> expected = List.of("Mr. Sole Trader", "Mr. John Rambo", "Both Defendants");
        List<String> actual = DefaultJudgmentUtils.getDefendants(caseData);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyList_whenBothRespondentsHaveRespondedToClaimIn1v2() {
        CaseData caseData = CaseDataBuilder.builder().atState1v2AndRespondentResponseDeadlinePassed().build().toBuilder()
            .respondent1ResponseDate(LocalDateTime.now()).respondent2ResponseDate(LocalDateTime.now()).build();
        List<String> actual = DefaultJudgmentUtils.getDefendants(caseData);
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnOneDefendant_whenRespondent1HasNotRespondedToClaimIn1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedAndReasponseDeadlinePassed1v1().build();
        List<String> expected = List.of("Mr. Sole Trader");
        List<String> actual = DefaultJudgmentUtils.getDefendants(caseData);
        assertThat(actual).isEqualTo(expected);
    }
}
