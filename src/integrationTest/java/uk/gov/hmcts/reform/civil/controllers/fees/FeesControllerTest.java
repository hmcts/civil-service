package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeVersionDto;
import uk.gov.hmcts.reform.fees.client.model.FlatAmountDto;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeesControllerTest extends BaseIntegrationTest {

    private static final String FEES_URL = "/fees/";

    @MockBean
    private FeesService feesService;

    @Test
    @SneakyThrows
    public void shouldReturnFeeRanges() {
        Fee2Dto[] response = buildFeeRangeResponse();
        when(feesService.getFeeRange()).thenReturn(response);
        doGet(BEARER_TOKEN, FEES_URL)
            .andExpect(content().json(toJson(response)))
            .andExpect(status().isOk());
    }

    private Fee2Dto[] buildFeeRangeResponse() {
        return new Fee2Dto[]{
            Fee2Dto
                .builder()
                .minRange(new BigDecimal("0.1"))
                .maxRange(new BigDecimal("300"))
                .currentVersion(FeeVersionDto
                                    .builder()
                                    .flatAmount(FlatAmountDto
                                                    .builder()
                                                    .amount(new BigDecimal("35"))
                                                    .build())
                                    .build())
                .build()
        };
    }
}
