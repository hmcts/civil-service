package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.checkIfDateDifferenceIsGreaterThan31Days;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.getCostOfJudgmentForDJ;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.getMoneyValue;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.getPartialPayment;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.isNonDivergent;

public class JudgmentsOnlineHelperTest {

    @Test
    void test_getRTLStatusBasedOnJudgementStatus() {
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.ISSUED)).isEqualTo("R");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.MODIFIED)).isEqualTo(
            "M");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.CANCELLED)).isEqualTo(
            "C");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.SET_ASIDE)).isEqualTo(
            "C");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.SATISFIED)).isEqualTo(
            "S");
    }

    @Test
    void test_validateIfFutureDate() {
        assertThat(JudgmentsOnlineHelper.validateIfFutureDate(LocalDate.now())).isFalse();
        assertThat(JudgmentsOnlineHelper.validateIfFutureDate(LocalDate.now().minusDays(3))).isFalse();
        assertThat(JudgmentsOnlineHelper.validateIfFutureDate(LocalDate.now().plusDays(3))).isTrue();
    }

    @Test
    void shouldCheckIfDateDifferenceIsGreaterThan31Days() {
        assertThat(checkIfDateDifferenceIsGreaterThan31Days(LocalDate.now(), LocalDate.now().plusDays(31))).isFalse();
        assertThat(checkIfDateDifferenceIsGreaterThan31Days(LocalDate.now(), LocalDate.now().plusDays(32))).isTrue();
    }

    @Test
    void checkIfNonDivergent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both Defendants")
                                                 .build())
                                      .build())
            .build();
        assertThat(isNonDivergent(caseData)).isTrue();
    }

    @Test
    void checkIfDivergent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("John Smith")
                                                 .build())
                                      .build())
            .build();
        assertThat(isNonDivergent(caseData)).isFalse();
    }

    @Test
    void testCostOfJudgmentForDJ() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .repaymentSummaryObject(
                "The judgment will order dsfsdf ffdg to pay £1072.00, "
                    + "including the claim fee and interest,"
                    + " if applicable, as shown:\n### Claim amount \n"
                    + " £1000.00\n ### Fixed cost amount"
                    + " \n£102.00\n### Claim fee amount \n £70.00\n ## "
                    + "Subtotal \n £1172.00\n\n ### Amount"
                    + " already paid \n£100.00\n ## Total still owed \n £1072.00")
            .build();
        assertThat(getCostOfJudgmentForDJ(caseData)).isEqualTo("172.00");
    }

    @Test
    void testPartialPayment() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .repaymentSummaryObject(
                "The judgment will order dsfsdf ffdg to pay £1072.00, "
                    + "including the claim fee and interest,"
                    + " if applicable, as shown:\n### Claim amount \n"
                    + " £1000.00\n ### Fixed cost amount"
                    + " \n£102.00\n### Claim fee amount \n £70.00\n ## "
                    + "Subtotal \n £1172.00\n\n ### Amount"
                    + " already paid \n£100.00\n ## Total still owed \n £1072.00")
            .partialPayment(YES)
            .partialPaymentAmount("15000")
            .build();

        assertThat(getPartialPayment(caseData)).isEqualTo("150.00");
    }

    @Test
    void testMoneyValue() {
        assertThat(getMoneyValue(null)).isEqualTo(BigDecimal.ZERO);
        assertThat(getMoneyValue("12.36")).isEqualTo("12.36");
    }
}
