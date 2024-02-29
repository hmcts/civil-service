package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@SpringBootTest(classes = {
    DashboardNotificationsParamsMapper.class,
    JacksonAutoConfiguration.class
})
public class DashboardNotificationsParamsMapperTest {

    @MockBean
    private FeesService feesService;
    @Autowired
    private DashboardNotificationsParamsMapper mapper;

    @BeforeEach
    void setup() {
        when(feesService.getFeeDataByTotalClaimAmount(any())).thenReturn(Fee.builder().calculatedAmountInPence(
            BigDecimal.valueOf(70000)).build());

    }

    @Test
    public void shouldMapAllParameters_WhenIsRequested() {

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee")
            .isEqualTo("£700");

        assertThat(result).extracting("ccdCaseReference")
            .isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime")
            .isEqualTo("4pm");

        assertThat(result).extracting("responseDeadline")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now().plusDays(14L)));

    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineIsNull() {

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData = caseData.toBuilder().respondent1ResponseDeadline(null).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference")
            .isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime")
            .isEqualTo("4pm");

        assertThat(result).extracting("responseDeadline")
            .isNull();

    }

}
