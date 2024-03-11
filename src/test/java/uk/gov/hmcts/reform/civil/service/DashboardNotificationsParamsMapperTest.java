package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        caseData = caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED).build();
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("responseDeadline")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now().plusDays(14L)));

        assertThat(result).extracting("defendantName")
            .isEqualTo(caseData.getRespondent1().getPartyName());
        assertThat(result).extracting("typeOfFee")
            .isEqualTo("claim");
    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineAndClaimFeeIsNull() {

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .claimFee(null).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("responseDeadline").isNull();

        assertThat(result).extracting("claimFee").isNull();

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

