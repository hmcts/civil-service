package uk.gov.hmcts.reform.civil.controllers.claims;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimsControllerTest extends BaseIntegrationTest {

    private static final String CLAIMS_URL = "/claims/{claimId}";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    @SneakyThrows
    public void shouldReturnOk() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
        when(coreCaseDataService.getCase(1L))
            .thenReturn(expectedCaseDetails);

        mockMvc.perform(
            get(CLAIMS_URL, 1L)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
