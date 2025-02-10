package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.CaseDefinitionConstants;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.service.AssignCaseService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.citizen.defendant.LipDefendantCaseAssignmentService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.service.search.CaseLegacyReferenceSearchService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CaseAssignmentControllerTest extends BaseIntegrationTest {

    private static final String CASES_URL = "/assignment";
    private static final String VALIDATE_PIN_URL = CASES_URL + "/reference/{caseReference}";
    private static final String VALIDATE_OCMC_PIN_URL = CASES_URL + "/reference/{caseReference}/ocmc";
    private static final String ASSIGN_CASE = CASES_URL + "/case/{caseId}/{caseRole}";

    private static final String DEPRECATED_DEFENDENT_LINK_CHECK_URL = CASES_URL + "/reference/{caseReference}/ocmc";
    private static final String DEFENDENT_LINK_CHECK_URL = CASES_URL + "/reference/{caseReference}/defendant-link-status";

    @MockBean
    private CaseLegacyReferenceSearchService caseByLegacyReferenceSearchService;
    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @MockBean
    private AssignCaseService assignCaseService;
    @MockBean
    private LipDefendantCaseAssignmentService lipDefendantCaseAssignmentService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectPinAndReference_whenValidateCaseAndPin_shouldReturnCaseData() {
        CaseDetails caseDetails = givenCaseIsFound();

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(content().json(toJson(caseDetails)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenIncorrectReference_whenValidateCaseAndPin_shouldReturnUnauthorized() {
        when(caseByLegacyReferenceSearchService
                 .getCaseDataByLegacyReference(any())).thenThrow(new SearchServiceCaseNotFoundException());

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenIncorrectPin_whenValidateCaseAndPin_shouldReturnBadRequest() {
        givenCaseIsFound();
        doThrow(new PinNotMatchException()).when(defendantPinToPostLRspecService).validatePin(
            any(CaseDetails.class),
            anyString()
        );

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenIncorrectPin_whenValidateOcmcPin_shouldReturnBadRequest() {
        givenCaseIsFound();
        doThrow(new PinNotMatchException()).when(defendantPinToPostLRspecService).validateOcmcPin(
            anyString(),
            anyString()
        );

        doPost("", "123", VALIDATE_OCMC_PIN_URL, "123")
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectParams_whenAssignClaim_shouldReturnStatusOk() {
        doPost("authorization", "", ASSIGN_CASE, "123", "RESPONDENTSOLICITORONE")
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectParams_whenAssignClaim_forDefendant_shouldReturnStatusOk() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);
        doPost("authorization", "12345", ASSIGN_CASE, "123", "DEFENDANT")
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectOcmcClaim_whenDefendantLinkedStatusFalse_shouldReturnStatusOk() {
        CaseDetails caseDetails = givenOcmcOrCivilCaseIsFound();
        caseDetails.setCaseTypeId(CaseDefinitionConstants.CMC_CASE_TYPE);
        when(defendantPinToPostLRspecService.isOcmcDefendantLinked(anyString())).thenReturn(false);
        boolean linkedStatus = false;

        doGet("", DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(linkedStatus);
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectOcmcClaim_whenDefendantLinkedStatusTrue_shouldReturnStatusOk() {
        CaseDetails caseDetails = givenOcmcOrCivilCaseIsFound();
        caseDetails.setCaseTypeId(CaseDefinitionConstants.CMC_CASE_TYPE);
        when(defendantPinToPostLRspecService.isOcmcDefendantLinked(anyString())).thenReturn(true);
        boolean linkedStatus = true;

        doGet("", DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(linkedStatus);
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectClaim_whenDefendantLinkedStatusFalse_shouldReturnStatusOk() {
        CaseDetails caseDetails = givenOcmcOrCivilCaseIsFound();
        caseDetails.setCaseTypeId(CaseDefinitionConstants.CASE_TYPE);
        when(defendantPinToPostLRspecService.isDefendantLinked(any())).thenReturn(false);
        boolean linkedStatus = false;

        doGet("", DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(linkedStatus);
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectClaim_whenDefendantLinkedStatusTrue_shouldReturnStatusOk() {
        CaseDetails caseDetails = givenOcmcOrCivilCaseIsFound();
        caseDetails.setCaseTypeId(CaseDefinitionConstants.CASE_TYPE);
        when(defendantPinToPostLRspecService.isDefendantLinked(any())).thenReturn(true);
        boolean linkedStatus = true;

        doGet("", DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(linkedStatus);
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void givenNoClaim_whenDefendantLinkedStatus_shouldReturnStatusOk() {
        when(caseByLegacyReferenceSearchService.getCivilOrOcmcCaseDataByCaseReference(any())).thenReturn(null);
        boolean linkedStatus = false;

        doGet("", DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString().equals(linkedStatus);
    }

    @Deprecated
    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectClaim_whenDefendantLinkedStatusFalse_shouldReturnStatusOk_DeprecatedEndpoint() {
        when(defendantPinToPostLRspecService.isOcmcDefendantLinked(anyString())).thenReturn(false);

        doGet("", DEPRECATED_DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk());
    }

    @Deprecated
    @Test
    @SneakyThrows
    @DirtiesContext
    void givenCorrectClaim_whenDefendantLinkedStatusTrue_shouldReturnStatusOk_DeprecatedEndpoint() {
        when(defendantPinToPostLRspecService.isOcmcDefendantLinked(anyString())).thenReturn(true);

        doGet("", DEPRECATED_DEFENDENT_LINK_CHECK_URL, "620MC123")
            .andExpect(status().isOk());
    }

    private CaseDetails givenCaseIsFound() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseByLegacyReferenceSearchService.getCaseDataByLegacyReference(any())).thenReturn(caseDetails);
        return caseDetails;
    }

    private CaseDetails givenOcmcOrCivilCaseIsFound() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseByLegacyReferenceSearchService.getCivilOrOcmcCaseDataByCaseReference(any())).thenReturn(caseDetails);
        return caseDetails;
    }

}
