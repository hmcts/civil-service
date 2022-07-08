package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.citizenui.CaseEventService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.ras.model.RoleAssignmentServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CasesControllerTest extends BaseIntegrationTest {

    private static final String CASES_URL = "/cases/{caseId}";
    private static final String CASES_ACTOR_URL = "/cases/actors/{actorId}";
    private static final String ACTORID = "1111111";
    private static final String CLAIMS_LIST_URL = "/cases/";
    private static final String ELASTICSEARCH = "{\n"
        + "\"terms\": {\n"
        + "\"reference\": [ \"1643728683977521\", \"1643642899151591\" ]\n"
        + "\n"
        + " }\n"
        + "}";
    private static final String CLAIMANT_CLAIMS_URL = "/cases/claimant/{submitterId}";
    private static final String DEFENDANT_CLAIMS_URL = "/cases/defendant/{submitterId}";
    private static final String GET_EVENT_TOKEN_URL = "/cases/defendant/{submitterId}/response/{caseId}/event-token";
    private static final List<DashboardClaimInfo> claimResults =
        Collections.singletonList(DashboardClaimInfo.builder()
                                      .claimAmount(new BigDecimal(
                                          "1000"))
                                      .claimNumber("4786")
                                      .claimantName(
                                          "Mr. James Bond")
                                      .defendantName(
                                          "Mr. Roger Moore")
                                      .responseDeadline(
                                          LocalDate.of(
                                              2022,
                                              1,
                                              1
                                          ))
                                      .build());
    private static final String EVENT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOi";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private RoleAssignmentsService roleAssignmentsService;

    @MockBean
    private ClaimStoreService claimStoreService;

    @MockBean
    private CaseEventService caseEventService;

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

    @Test
    @SneakyThrows
    public void shouldReturnRASAssignment() {
        var rasResponse = RoleAssignmentServiceResponse
            .builder()
            .roleAssignmentResponse(
                List.of(RoleAssignmentResponse
                            .builder()
                            .actorId(ACTORID)
                            .build()
                )
            )
            .build();

        when(roleAssignmentsService.getRoleAssignments(anyString(), anyString()))
            .thenReturn(rasResponse);
        doGet(BEARER_TOKEN, CASES_ACTOR_URL, ACTORID)
            .andExpect(content().json(toJson(rasResponse)))
            .andExpect(status().isOk());

    }

    @Test
    @SneakyThrows
    public void shouldReturnListOfCasesHttp200() {
        SearchResult expectedCaseDetails = SearchResult.builder()
            .total(1)
            .cases(Arrays
                       .asList(CaseDetails
                                   .builder()
                                   .id(1L)
                                   .build()))
            .build();

        SearchResult expectedCaseData = SearchResult.builder()
            .total(1)
            .cases(Arrays.asList(CaseDetails.builder().id(1L).build()))
            .build();

        when(coreCaseDataService.searchCases(any(), anyString()))
            .thenReturn(expectedCaseDetails);
        doPost(BEARER_TOKEN, ELASTICSEARCH, CLAIMS_LIST_URL, "")
            .andExpect(content().json(toJson(expectedCaseData)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnClaimsForClaimantSuccessfully() {
        when(claimStoreService.getClaimsForClaimant(any(), any())).thenReturn(claimResults);
        doGet(BEARER_TOKEN, CLAIMANT_CLAIMS_URL, "123")
            .andExpect(content().json(toJson(claimResults)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnClaimsForDefendantSuccessfully() {
        when(claimStoreService.getClaimsForDefendant(any(), any())).thenReturn(claimResults);
        doGet(BEARER_TOKEN, DEFENDANT_CLAIMS_URL, "123")
            .andExpect(content().json(toJson(claimResults)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnEventTokenSuccessfully() {
        when(caseEventService.getDefendantResponseSpecEventToken(any(), any(), any())).thenReturn(EVENT_TOKEN);
        doGet(BEARER_TOKEN, GET_EVENT_TOKEN_URL, "1213", "123")
            .andExpect(content().string(EVENT_TOKEN))
            .andExpect(status().isOk());
    }
}
