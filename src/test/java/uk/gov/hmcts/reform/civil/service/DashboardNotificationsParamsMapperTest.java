package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class DashboardNotificationsParamsMapperTest {

    private DashboardNotificationsParamsMapper mapper;

    private CaseData caseData;

    @BeforeEach
    void setup() {
        mapper = new DashboardNotificationsParamsMapper();
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
    }

    @Test
    public void shouldMapAllParameters_WhenIsRequested() {

        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);

        LocalDateTime applicant1ResponseDeadline = LocalDateTime.of(2024, 3, 21, 16, 0);
        caseData = caseData.toBuilder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .totalClaimAmount(BigDecimal.valueOf(124.67))
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100))
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(date))
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now())
            .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
            .caseDataLiP(CaseDataLiP.builder().applicant1ClaimSettledDate(LocalDate.now()).build())
            .applicant1ResponseDeadline(applicant1ResponseDeadline)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("defendantAdmittedAmount").isEqualTo("£100");

        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadlineEn")
            .isEqualTo(DateUtils.formatDate(date));

        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(date));

        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(applicant1ResponseDeadline.toLocalDate()));
        assertThat(result).extracting("applicant1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(applicant1ResponseDeadline.toLocalDate()));
        assertThat(result).extracting("respondent1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1PartyName")
            .isEqualTo(caseData.getRespondent1().getPartyName());

        assertThat(result).extracting("typeOfFee").isEqualTo("claim");
        assertThat(result).extracting("respondent1SettlementAgreementDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now()));

        assertThat(result).extracting("respondent1SettlementAgreementDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now()));

        assertThat(result).extracting("claimantSettlementAgreement").isEqualTo("accepted");
        assertThat(result).extracting("applicant1ClaimSettledDateEn")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now()));

        assertThat(result).extracting("applicant1ClaimSettledDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now()));

        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo("21 March 2024");
        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo("21 March 2024");
    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineAndClaimFeeIsNull() {

        caseData = caseData.toBuilder()
            .respondent1ResponseDeadline(null)
            .respondToAdmittedClaimOwingAmountPounds(null)
            .respondToClaimAdmitPartLRspec(null)
            .respondent1ResponseDeadline(null)
            .claimFee(null)
            .respondent1RespondToSettlementAgreementDeadline(null)
            .applicant1ResponseDeadline(null)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).doesNotContainKey("respondent1ResponseDeadlineEn");
        assertThat(result).doesNotContainKey("respondent1ResponseDeadlineCy");

        assertThat(result).doesNotContainKey("defendantAdmittedAmount");

        assertThat(result).doesNotContainKey("defendantAdmittedAmount");

        assertThat(result).doesNotContainKey("respondent1AdmittedAmountPaymentDeadlineEn");
        assertThat(result).doesNotContainKey("respondent1AdmittedAmountPaymentDeadlineCy");

        assertThat(result).doesNotContainKey("respondent1SettlementAgreementDeadlineEn");
        assertThat(result).doesNotContainKey("respondent1SettlementAgreementDeadlineCy");

        assertThat(result).doesNotContainKey("claimFee");

        assertThat(result).doesNotContainKey("applicant1ResponseDeadlineEn");
        assertThat(result).doesNotContainKey("applicant1ResponseDeadlineEn");
    }

    @Test
    public void shouldMapCaseSettleAmountAndCaseSettledDateWheResponseTypeIsFullDefence() {

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(new BigDecimal("100050"))
                                .whenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                                .build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimSettledAmount").isEqualTo("£1000.50");
        assertThat(result).extracting("claimSettledDateEn").isEqualTo("29 March 2023");
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 Mawrth 2023");
    }

    @Test
    public void shouldMapCaseSettleAmountAndCaseSettledDateWhenResponseTypeIsPartAdmit() {

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaim(RespondToClaim.builder()
                                        .howMuchWasPaid(new BigDecimal("100055"))
                                        .whenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                                        .build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimSettledAmount").isEqualTo("£1000.55");
        assertThat(result).extracting("claimSettledDateEn").isEqualTo("29 March 2023");
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 Mawrth 2023");
    }

    @Test
    public void shouldMapParameters_whenHwFPartRemissionGranted() {
        caseData = caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().remissionAmount(BigDecimal.valueOf(2500))
                                       .outstandingFeeInPounds(BigDecimal.valueOf(100)).build()).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimIssueRemissionAmount").isEqualTo("£25");
        assertThat(result).extracting("claimIssueOutStandingAmount").isEqualTo("£100");
    }

    @Test
    public void shouldMapParameters_whenClaimantSubmitSettlmentEvent() {
        caseData = caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED)
            .caseDataLiP(CaseDataLiP.builder().applicant1ClaimSettledDate(LocalDate.of(2024, 03, 19)).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicant1ClaimSettledDateEn").isEqualTo("19 March 2024");
        assertThat(result).extracting("applicant1ClaimSettledDateCy").isEqualTo("19 Mawrth 2024");
    }

    @Test
    public void shouldMapParameters_whenRepaymentPlanIsSet() {
        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);

        caseData = caseData.toBuilder().respondent1RepaymentPlan(
            RepaymentPlanLRspec.builder()
                .firstRepaymentDate(date)
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                .paymentAmount(new BigDecimal(1000))
                .build()).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("instalmentAmount").isEqualTo("£10");
        assertThat(result).extracting("instalmentTimePeriodEn").isEqualTo("week");
        assertThat(result).extracting("instalmentTimePeriodCy").isEqualTo("week");
        assertThat(result).extracting("instalmentStartDateEn").isEqualTo(DateUtils.formatDate(date));
        assertThat(result).extracting("instalmentStartDateCy").isEqualTo(DateUtils.formatDateInWelsh(date));
    }

    @Test
    public void shouldMapParameters_whenHearingDueDate() {
        LocalDate date = LocalDate.of(2024, Month.MARCH, 22);
        caseData = caseData.toBuilder().hearingDueDate(date)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("hearingDueDateEn").isEqualTo("22 March 2024");
        assertThat(result).extracting("hearingDueDateCy").isEqualTo("22 March 2024");
    }
}

