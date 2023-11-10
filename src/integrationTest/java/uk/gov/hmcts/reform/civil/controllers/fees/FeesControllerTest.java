package uk.gov.hmcts.reform.civil.controllers.fees;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeVersionDto;
import uk.gov.hmcts.reform.fees.client.model.FlatAmountDto;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeesControllerTest extends BaseIntegrationTest {

    private static final String FEES_RANGES_URL = "/fees/ranges/";
    private static final String FEES_CLAIM_URL = "/fees/claim/{claimAmount}";
    private static final String FEES_HEARING_URL = "/fees/hearing/{claimAmount}";

    @MockBean
    private FeesService feesService;

    @Test
    @SneakyThrows
    public void shouldReturnClaimFee() {
        Fee response = buildFeeResponse();
        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal("7000"))).thenReturn(response);
        doGet(BEARER_TOKEN, FEES_CLAIM_URL, 7000)
            .andExpect(content().json(toJson(response)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldReturnHearingFee() {
        Fee response = buildFeeResponse();
        when(feesService.getHearingFeeDataByTotalClaimAmount(new BigDecimal("7000"))).thenReturn(response);
        doGet(BEARER_TOKEN, FEES_HEARING_URL, 7000)
            .andExpect(content().json(toJson(response)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldReturnFeeRanges() {
        Fee2Dto[] response = buildFeeRangeResponse();
        when(feesService.getFeeRange()).thenReturn(List.of(response));
        doGet(BEARER_TOKEN, FEES_RANGES_URL)
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

    private Fee buildFeeResponse() {
        return new Fee(new BigDecimal("1000"), "123", "1");
    }

}
