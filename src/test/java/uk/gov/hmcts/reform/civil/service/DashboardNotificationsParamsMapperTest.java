package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.math.BigDecimal;
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
    void setup(){

        when(feesService.getFeeDataByTotalClaimAmount(any())).thenReturn(Fee.builder().calculatedAmountInPence(
            BigDecimal.valueOf(70)).build());

    }

    @Test
    public void shouldMapAllParameters_WhenIsRequested(){

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference")
            .isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime")
            .isEqualTo("4pm");

        assertThat(result).extracting("responseDeadline")
            .isEqualTo("12 March 2024");

    }
}
