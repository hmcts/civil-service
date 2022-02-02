package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CasesControllerTest extends BaseIntegrationTest {

    private static final String CLAIMS_URL = "/cases/{cid}";
    private static final String CLAIMS_LIST_URL = "/cases/";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    @SneakyThrows
    public void shouldReturnHttp200() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
        CaseData expectedCaseData = CaseData.builder().ccdCaseReference(1L).build();

        when(coreCaseDataService.getCase(1L))
            .thenReturn(expectedCaseDetails);
        when(caseDetailsConverter.toCaseData(expectedCaseDetails.getData()))
            .thenReturn(expectedCaseData);
        doGet(BEARER_TOKEN, CLAIMS_URL, 1L)
            .andExpect(content().json(toJson(expectedCaseData)))
            .andExpect(status().isOk());
    }
    @Test
    @SneakyThrows
    public void shouldReturnListOfCasesHttp200() {
        SearchResult expectedCaseDetails = SearchResult.builder().total(1).cases(Arrays.asList(CaseDetails.builder().id(1L).build())).build();
        SearchResult expectedCaseData = SearchResult.builder().total(1).build();
        String elasticsearch = "{\n" +
            "\n" +
            "    \"terms\": {\n" +
            "          \"id\": [ \"1\"]\n" +
            "\n" +
            "    }   \n" +
            "  \n" +
            "}";
        when(coreCaseDataService.searchCases(any()))
            .thenReturn(expectedCaseDetails);
        doPost(BEARER_TOKEN, elasticsearch, CLAIMS_URL, "")
            .andExpect(content().json(toJson(expectedCaseData)))
            .andExpect(status().isOk());
    }
}
