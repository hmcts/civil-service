package uk.gov.hmcts.reform.civil.controllers.fees;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.model.citizenui.GeneralApplicationFeeRequest;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeesControllerTest extends BaseIntegrationTest {

    private static final String FEES_RANGES_URL = "/fees/ranges/";
    private static final String FEES_CLAIM_URL = "/fees/claim/{claimAmount}";
    private static final String FEES_CLAIM_INTEREST_TO_DATE_URL = "/fees/claim/interest-to-date";
    private static final String FEES_HEARING_URL = "/fees/hearing/{claimAmount}";
    private static final String FEES_GA_URL = "/fees/general-application";

    @MockBean
    private FeesService feesService;

    @MockBean
    private InterestCalculator interestCalculator;

    @MockBean
    private GeneralAppFeesService gaFeesService;
    private ObjectMapper objectMapper = new ObjectMapper();

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
    public void shouldReturnClaimInterestToDate() {
        CaseData caseData = CaseData.builder().build();
        when(interestCalculator.calculateInterest(caseData)).thenReturn(new BigDecimal("0.1"));
        doGet(BEARER_TOKEN, FEES_CLAIM_INTEREST_TO_DATE_URL, caseData)
            .andExpect(content().json(toJson("0.1")))
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
    public void shouldReturnGeneralApplicationFee() {
        Fee response = buildFeeResponse();
        when(gaFeesService.getFeeForGALiP(List.of(GeneralApplicationTypes.EXTEND_TIME), true, false, null)).thenReturn(
            response);
        GeneralApplicationFeeRequest feeRequest =
            GeneralApplicationFeeRequest.builder().applicationTypes((List.of(GeneralApplicationTypes.EXTEND_TIME)))
                .withConsent(true).withNotice(false).build();
        mockMvc.perform(
                MockMvcRequestBuilders.post(FEES_GA_URL)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                    .content(objectMapper.writeValueAsString(feeRequest))
                    .contentType(MediaType.APPLICATION_JSON))
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
