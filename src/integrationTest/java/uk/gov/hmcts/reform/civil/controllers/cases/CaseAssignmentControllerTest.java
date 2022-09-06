package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.service.search.CaseLegacyReferenceSearchService;
import uk.gov.hmcts.reform.civil.service.search.exceptions.CaseNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CaseAssignmentControllerTest extends BaseIntegrationTest {

    private static final String CASES_URL = "/assignment";
    private static final String VALIDATE_PIN_URL = CASES_URL + "/reference/{caseReference}";

    @MockBean
    private CaseLegacyReferenceSearchService caseByLegacyReferenceSearchService;
    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

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
    void givenIncorrectReference_whenValidateCaseAndPin_shouldReturnBadRequest() {
        when(caseByLegacyReferenceSearchService
                 .getCaseDataByLegacyReference(any())).thenThrow(new CaseNotFoundException());

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void givenIncorrectPin_whenValidateCaseAndPin_shouldReturnUnauthorised() {
        givenCaseIsFound();
        doThrow(new PinNotMatchException()).when(defendantPinToPostLRspecService).validatePin(
            any(CaseDetails.class),
            anyString()
        );

        doPost("", "123", VALIDATE_PIN_URL, "123")
            .andExpect(status().isUnauthorized());
    }

    private CaseDetails givenCaseIsFound() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseByLegacyReferenceSearchService.getCaseDataByLegacyReference(any())).thenReturn(caseDetails);
        return caseDetails;
    }

}
