package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);

        caseData = caseData.toBuilder().respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100))
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder().whenWillThisAmountBePaid(date).build())
            .hwfFeeType(FeeType.CLAIMISSUED).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("defendantAdmittedAmount").isEqualTo("100");

        assertThat(result).extracting("defendantAdmittedAmountPaymentDeadline").isEqualTo("22nd February 2024");

        assertThat(result).extracting("responseDeadline");
        assertThat(result).extracting("respondent1ResponseDeadline")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now().plusDays(14L)));

        assertThat(result).extracting("respondent1PartyName")
            .isEqualTo(caseData.getRespondent1().getPartyName());
        assertThat(result).extracting("typeOfFee")
            .isEqualTo("claim");
    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineAndClaimFeeIsNull() {

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondToAdmittedClaimOwingAmountPounds(null)
            .respondToClaimAdmitPartLRspec(null)
            .claimFee(null).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("responseDeadline").isNull();

        assertThat(result).extracting("defendantAdmittedAmount").isNull();

        assertThat(result).extracting("defendantAdmittedAmountPaymentDeadline").isNull();

        assertThat(result).extracting("claimFee").isNull();

    }
}

