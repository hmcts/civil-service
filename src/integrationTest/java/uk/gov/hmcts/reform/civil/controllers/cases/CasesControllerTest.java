package uk.gov.hmcts.reform.civil.controllers.cases;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataInvalidException;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.UserNotFoundOnCaseException;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.UserNotFoundOnCaseException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bulkclaims.CaseworkerSubmitEventDTo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseworkerCaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.repaymentplan.RepaymentPlanDecisionService;
import uk.gov.hmcts.reform.civil.service.citizenui.DashboardClaimInfoService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.service.search.CaseSdtRequestSearchService;
import uk.gov.hmcts.reform.civil.service.user.UserInformationService;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT;

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
    private static final String CASEWORKER_SUBMIT_EVENT_URL = "/cases/caseworkers/create-case/{userId}";
    private static final String CASEWORKER_SEARCH_CASE_URL = "/cases/caseworker/searchCaseForSDT/{userId}?sdtRequestId=isUnique";
    private static final String VALIDATE_POSTCODE_URL = "/cases/caseworker/validatePin/?postCode=rfft";

    private static final String CALCULATE_DEADLINE_URL = "/cases/response/deadline";
    private static final String AGREED_RESPONSE_DEADLINE_DATE_URL = "/cases/response/agreeddeadline/{claimId}";
    private static final String USER_CASE_ROLES = "/cases/{caseId}/userCaseRoles";
    private static final String COURT_DECISION_URL = "/cases/{caseId}/courtDecision";
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
    private CaseSdtRequestSearchService caseSdtRequestSearchService;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @MockBean
    PostcodeValidator postcodeValidator;

    @MockBean
    private UserInformationService userInformationService;

    @MockBean
    private RepaymentPlanDecisionService repaymentPlanDecisionService;

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
        var dashBoardResponse = DashboardResponse.builder().totalPages(1).claims(claimResults).build();
        when(dashboardClaimInfoService.getDashboardClaimantResponse(any(), any(), eq(1))).thenReturn(dashBoardResponse);
        doGet(BEARER_TOKEN, CLAIMANT_CLAIMS_URL, "123")
            .andExpect(content().json(toJson(dashBoardResponse)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnClaimsForDefendantSuccessfully() {
        var dashBoardResponse = DashboardResponse.builder().totalPages(1).claims(claimResults).build();
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
        System.out.println("Ex :"+extensionDate);
        when(deadlineExtensionCalculatorService.calculateExtendedDeadline(any(), anyInt())).thenReturn(extensionDate);
        String jsonString = "{\"extendedDeadline\":\"2022-06-06\", \"plusDays\":5}";
        JSONObject json = new JSONObject(jsonString);
        System.out.println("Ex :"+json);
        doPost(
            BEARER_TOKEN,
            json,
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

    @Test
    @SneakyThrows
    void shouldSearchCaseSuccessfullyForCaseWorker_whenCaseExists() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseSdtRequestSearchService.searchCaseForSdtRequest(any())).thenReturn(Arrays.asList(caseDetails));

        doGet(
            BEARER_TOKEN,
            CASEWORKER_SEARCH_CASE_URL,
            "sdtRequest",
            "userId"

        )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(false);

    }

    @Test
    @SneakyThrows
    void shouldSearchCaseSuccessfullyForCaseWorker_whenCaseNotExists() {

        when(caseSdtRequestSearchService.searchCaseForSdtRequest(any())).thenReturn(Lists.newArrayList());

        doGet(
            BEARER_TOKEN,
            CASEWORKER_SEARCH_CASE_URL,
            "sdtRequest",
            "userId"

        )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(true);

    }

    @Test
    @SneakyThrows
    void shouldValidatePostCodeSuccessfullyWhenInEnglandOrWales() {

        when(postcodeValidator.validate(any())).thenReturn(Lists.newArrayList());

        doGet(
            BEARER_TOKEN,
            VALIDATE_POSTCODE_URL
        )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(Lists.newArrayList());
    }

    @Test
    @SneakyThrows
    void shouldValidatePostCodeAndSendErrorsWhenNotInEnglandOrWales() {

        when(postcodeValidator.validate(any())).thenReturn(
            Lists.newArrayList("Postcode must be in England or Wales"));

        doGet(
            BEARER_TOKEN,
            VALIDATE_POSTCODE_URL
        )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(
                Arrays.asList("Postcode must be in England or Wales"));
    }

    @Test
    @SneakyThrows
    void shouldGetUserInfoSuccessfully() {
        List<String> expectedRoles = List.of("role1", "role2");
        when(userInformationService.getUserCaseRoles(anyString(), anyString()))
            .then(invocation -> expectedRoles);
        doGet(
            BEARER_TOKEN,
            USER_CASE_ROLES,
            "1"
        )
            .andExpect(status().isOk())
            .andExpect(content().json(toJson(expectedRoles)))
            .andReturn();

    }

    @Test
    @SneakyThrows
    void shouldThrowNotFoundExceptionWhenGetUserInfo() {
        when(userInformationService.getUserCaseRoles(anyString(), anyString()))
            .thenThrow(CaseNotFoundException.class);

        doGet(
            BEARER_TOKEN,
            USER_CASE_ROLES,
            "1"
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Case was not found"))
            .andReturn();

    }

    @Test
    @SneakyThrows
    void shouldThrowUserNotFoundOnCaseExceptionWhenRolesIsEmpty() {
        when(userInformationService.getUserCaseRoles(anyString(), anyString()))
            .thenThrow(new UserNotFoundOnCaseException("111"));

        doGet(
            BEARER_TOKEN,
            USER_CASE_ROLES,
            "1"
        )
            .andExpect(status().isNotFound())
            .andExpect(content().string("User with Id: 111 was not found on case"))
            .andReturn();

    }

    @Test
    @SneakyThrows
    void shouldReturnDecisionMadeForTheClaimantRepaymentPlan() {
        //Given
        given(repaymentPlanDecisionService.getCalculatedDecision(any(), any())).willReturn(IN_FAVOUR_OF_CLAIMANT);
        //When
        doPost(BEARER_TOKEN, ClaimantProposedPlan.builder().proposedRepaymentType(IMMEDIATELY).build(), COURT_DECISION_URL, "1")
            .andExpect(status().isOk());
    }

}
