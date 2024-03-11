package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardNotificationsParamsMapperTest {

    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private  FeesService feesService;
    private CaseData caseData;

    @BeforeEach
    void setup() {
        mapper = new DashboardNotificationsParamsMapper(feesService);
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        when(feesService.getFeeDataByTotalClaimAmount(any()))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal("100")).build());
    }

    @Test
    public void shouldMapAllParameters_WhenIsRequested() {
        caseData = caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED).build();
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("respondent1ResponseDeadline")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now().plusDays(14L)));

        assertThat(result).extracting("respondent1PartyName")
            .isEqualTo(caseData.getRespondent1().getPartyName());
        assertThat(result).extracting("typeOfFee")
            .isEqualTo("claim");
    }

}

