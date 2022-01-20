package uk.gov.hmcts.reform.civil.controllers.claims;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ClaimsControllerTest extends BaseIntegrationTest {

    private static final String CLAIMS_URL = "/claims/{claimId}";
    private static final String CLAIMS_LIST_URL = "/claims/list";

    @Test
    @SneakyThrows
    public void shouldReturnOk() {
        mockMvc.perform(get(CLAIMS_URL, 1).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk());


    }
    @Test
    @SneakyThrows
    public void shouldReturnListOk() {
        mockMvc.perform(get(CLAIMS_LIST_URL, 1).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk());

    }
}
