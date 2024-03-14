package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

        LocalDate date = LocalDate.of(2024, Month.JANUARY, 11);

        caseData = caseData.toBuilder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .totalClaimAmount(BigDecimal.valueOf(124.67))
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100))
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(date))
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("defendantAdmittedAmount").isEqualTo("100");

        assertThat(result).extracting("defendantAdmittedAmountPaymentDeadlineEn")
            .isEqualTo(DateUtils.formatDate(date));

        assertThat(result).extracting("defendantAdmittedAmountPaymentDeadlineCy")
            .isEqualTo(DateUtils.formatDate(date));

        assertThat(result).extracting("respondent1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDate(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1PartyName")
            .isEqualTo(caseData.getRespondent1().getPartyName());

        assertThat(result).extracting("typeOfFee").isEqualTo("claim");
    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineAndClaimFeeIsNull() {

        caseData = caseData.toBuilder()
            .respondent1ResponseDeadline(null)
            .respondToAdmittedClaimOwingAmountPounds(null)
            .respondToClaimAdmitPartLRspec(null)
            .respondent1ResponseDeadline(null)
            .claimFee(null)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("respondent1ResponseDeadlineEn").isNull();
        assertThat(result).extracting("respondent1ResponseDeadlineCy").isNull();

        assertThat(result).extracting("defendantAdmittedAmount").isNull();

        assertThat(result).extracting("defendantAdmittedAmountPaymentDeadlineEn").isNull();
        assertThat(result).extracting("defendantAdmittedAmountPaymentDeadlineEnCy").isNull();

        assertThat(result).extracting("claimFee").isNull();

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
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 March 2023");
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
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 March 2023");
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
}

