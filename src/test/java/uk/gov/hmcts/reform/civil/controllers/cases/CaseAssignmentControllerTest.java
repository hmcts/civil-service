package uk.gov.hmcts.reform.civil.controllers.cases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.PinDto;
import uk.gov.hmcts.reform.civil.service.AssignCaseService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.citizen.defendant.LipDefendantCaseAssignmentService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.search.CaseLegacyReferenceSearchService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentControllerTest {

    private static final String AUTHORISATION = "Bearer auth";
    private static final String CASE_ID = "123";
    private static final String PIN = "12345";

    @Mock
    private CaseLegacyReferenceSearchService caseByLegacyReferenceSearchService;
    @Mock
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @Mock
    private AssignCaseService assignCaseService;
    @Mock
    private LipDefendantCaseAssignmentService lipDefendantCaseAssignmentService;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private CaseAssignmentController controller;

    @Test
    void shouldReturnConflictAndSkipAssignment_whenDefendantClaimIsFinalised() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .state(CASE_SETTLED.name())
            .build();
        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);

        ResponseEntity<String> response = controller.assignCaseToDefendant(
            AUTHORISATION,
            CASE_ID,
            Optional.of(CaseRole.DEFENDANT),
            Optional.of(new PinDto(PIN))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("CLAIM_ALREADY_FINALISED");
        verify(defendantPinToPostLRspecService).validatePin(caseDetails, PIN);
        verify(assignCaseService, never()).assignCase(anyString(), anyString(), any());
        verify(lipDefendantCaseAssignmentService, never())
            .addLipDefendantToCaseDefendantUserDetails(anyString(), anyString(), any(), any());
    }
}
