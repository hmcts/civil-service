package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CasesControllerTest extends BaseIntegrationTest {

    private static final String CASES_URL = "/cases/{caseId}";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    @SneakyThrows
    public void shouldReturnHttp200() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
        CaseData expectedCaseData = CaseData.builder().ccdCaseReference(1L).build();

        when(coreCaseDataService.getCase(1L, BEARER_TOKEN))
            .thenReturn(expectedCaseDetails);
        when(caseDetailsConverter.toCaseData(expectedCaseDetails.getData()))
            .thenReturn(expectedCaseData);
        doGet(BEARER_TOKEN, CASES_URL, 1L)
            .andExpect(content().json(toJson(expectedCaseData)))
            .andExpect(status().isOk());
    }
}
