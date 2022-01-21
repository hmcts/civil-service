package uk.gov.hmcts.reform.civil.controllers.claims;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimsControllerTest extends BaseIntegrationTest {

    private static final String CLAIMS_URL = "/claims/{claimId}";
    private static final String CLAIMS_LIST_URL = "/claims/list";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    @SneakyThrows
    public void shouldReturnHttp200() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
        when(coreCaseDataService.getCase(1L))
            .thenReturn(expectedCaseDetails);

        doGet(BEARER_TOKEN, CLAIMS_LIST_URL, 1L)
            .andExpect(status().isOk());
    }
    @Test
    @SneakyThrows
    public void shouldReturnOk() {
        doGet(BEARER_TOKEN, CLAIMS_LIST_URL)
            .andExpect(status().isOk());
    }
}
