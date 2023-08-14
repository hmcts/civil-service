package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataInvalidException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bulkclaims.CaseworkerSubmitEventDTo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardDefendantResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerCaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizenui.DashboardClaimInfoService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private static final String DEFENDANT_CLAIMS_URL = "/cases/defendant/{submitterId}?page=1";
    private static final String SUBMIT_EVENT_URL = "/cases/{caseId}/citizen/{submitterId}/event";
    private static final String CASEWORKER_SUBMIT_EVENT_URL = "/cases/caseworkers/jurisdictions/{jurisdictionId}/case-types/{caseType}/cases/{userId}";
    private static final String CALCULATE_DEADLINE_URL = "/cases/response/deadline";
    private static final String AGREED_RESPONSE_DEADLINE_DATE_URL = "/cases/response/agreeddeadline/{claimId}";
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
    private DashboardClaimInfoService dashboardClaimInfoService;

    @MockBean
    private CaseEventService caseEventService;

    @MockBean
    private CaseworkerCaseEventService caseworkerCaseEventService;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @Test
    @SneakyThrows
    public void shouldReturnHttp200() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();

        when(coreCaseDataService.getCase(1L, BEARER_TOKEN))
            .thenReturn(expectedCaseDetails);
        doGet(BEARER_TOKEN, CASES_URL, 1L)
            .andExpect(content().json(toJson(expectedCaseDetails)))
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
        when(dashboardClaimInfoService.getClaimsForClaimant(any(), any())).thenReturn(claimResults);
        doGet(BEARER_TOKEN, CLAIMANT_CLAIMS_URL, "123")
            .andExpect(content().json(toJson(claimResults)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnClaimsForDefendantSuccessfully() {
        var dashBoardResponse = DashboardDefendantResponse.builder().totalPages(1).claims(claimResults).build();
        when(dashboardClaimInfoService.getDashboardDefendantResponse(
            any(),
            any(),
            eq(1)
        )).thenReturn(dashBoardResponse);
        doGet(BEARER_TOKEN, DEFENDANT_CLAIMS_URL, "123")
            .andExpect(content().json(toJson(dashBoardResponse)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldSubmitEventSuccessfully() {
        CaseDetails expectedCaseDetails = CaseDetails.builder().id(1L).build();
        when(caseEventService.submitEvent(any())).thenReturn(expectedCaseDetails);
        doPost(
            BEARER_TOKEN,
            EventDto.builder().event(CaseEvent.DEFENDANT_RESPONSE_SPEC).caseDataUpdate(Map.of()).build(),
            SUBMIT_EVENT_URL,
            "123",
            "123"
        ).andExpect(content().json(toJson(expectedCaseDetails)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldCalculateDeadlineSuccessfully() {
        LocalDate extensionDate = LocalDate.of(2022, 6, 6);
        when(deadlineExtensionCalculatorService.calculateExtendedDeadline(any())).thenReturn(extensionDate);
        doPost(
            BEARER_TOKEN,
            extensionDate,
            CALCULATE_DEADLINE_URL
        )
            .andExpect(content().json(toJson(extensionDate)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnDeadlineExtensionAgreedDate() {
        LocalDate agreedDate = LocalDate.of(2023, 4, 22);
        when(coreCaseDataService.getAgreedDeadlineResponseDate(any(), any())).thenReturn(agreedDate);
        doGet(BEARER_TOKEN, AGREED_RESPONSE_DEADLINE_DATE_URL, 1L)
            .andExpect(content().json(toJson(agreedDate)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldSubmitEventSuccessfullyForCaseWorker() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseData caseData = CaseData.builder().ccdCaseReference(1990L).build();
        when(caseworkerCaseEventService.submitEventForNewClaimCaseWorker(any())).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails))
            .thenReturn(caseData);
        doPost(
            BEARER_TOKEN,
            CaseworkerSubmitEventDTo.builder().event(CaseEvent.CREATE_CLAIM_SPEC).data(Map.of()).build(),
            CASEWORKER_SUBMIT_EVENT_URL,
            "userId",
            "jurisdictionId",
            "caseTypeId"
        )
            .andExpect(content().json(toJson(caseDetails)))
            .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void shouldNotSubmitEventSuccessfullyForisUnauthorizedCaseWorker() {
        doPost(
            "invalid token",
            CaseworkerSubmitEventDTo.builder().event(CaseEvent.CREATE_CLAIM_SPEC).data(Map.of()).build(),
            CASEWORKER_SUBMIT_EVENT_URL,
            "userId",
            "jurisdictionId",
            "caseTypeId"
        )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void shouldThrowUnprocessableEntityExceptionCaseWorkerSubmit() {
        when(caseworkerCaseEventService.submitEventForNewClaimCaseWorker(any()))
            .thenThrow(CaseDataInvalidException.class);

        doPost(
             BEARER_TOKEN,
             CaseworkerSubmitEventDTo.builder().event(CaseEvent.CREATE_CLAIM_SPEC).data(Map.of()).build(),
             CASEWORKER_SUBMIT_EVENT_URL,
             "userId",
             "jurisdictionId",
             "caseTypeId"
        )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string("Submit claim unsuccessful, Invalid Case data"))
            .andReturn();

    }

}
