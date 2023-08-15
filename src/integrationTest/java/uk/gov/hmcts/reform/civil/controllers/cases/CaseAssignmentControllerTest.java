package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.service.AssignCaseService;
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
    private static final String ASSIGN_CASE = CASES_URL + "/case/{caseId}/{caseRole}";

    @MockBean
    private CaseLegacyReferenceSearchService caseByLegacyReferenceSearchService;
    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @MockBean
    private AssignCaseService assignCaseService;
    @MockBean
    private LipDefendantCaseAssignmentService lipDefendantCaseAssignmentService;

    @Test
    @SneakyThrows
    void givenCorrectPinAndReference_whenValidateCaseAndPin_shouldReturnCaseData() {
        CaseDetails caseDetails = givenCaseIsFound();

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(content().json(toJson(caseDetails)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void givenIncorrectReference_whenValidateCaseAndPin_shouldReturnUnauthorized() {
        when(caseByLegacyReferenceSearchService
                 .getCaseDataByLegacyReference(any())).thenThrow(new SearchServiceCaseNotFoundException());

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
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
    void givenCorrectParams_whenAssignClaim_shouldReturnStatusOk() {
        doPost("authorization", "", ASSIGN_CASE, "123", "RESPONDENTSOLICITORONE")
            .andExpect(status().isOk());
    }

    private CaseDetails givenCaseIsFound() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseByLegacyReferenceSearchService.getCaseDataByLegacyReference(any())).thenReturn(caseDetails);
        return caseDetails;
    }

}
