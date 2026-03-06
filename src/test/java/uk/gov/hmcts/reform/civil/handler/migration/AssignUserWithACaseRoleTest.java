package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.AssignCaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

class AssignUserWithACaseRoleTest {

    @Mock
    private CoreCaseUserService coreCaseUserService;

    private AssignUserWithACaseRole assignUserWithACaseRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        assignUserWithACaseRole = new AssignUserWithACaseRole(coreCaseUserService);
    }

    @Test
    void shouldAssignCaseRoleSuccessfully() {
        CaseData caseData = CaseData.builder().build();

        AssignCaseReference assignCaseReference = new AssignCaseReference();
        assignCaseReference.setCaseReference("12345");
        assignCaseReference.setUserId("user1");
        assignCaseReference.setOrganisationId("org1");
        assignCaseReference.setCaseRole("CREATOR");

        assignUserWithACaseRole.migrateCaseData(caseData, assignCaseReference);

        verify(coreCaseUserService).assignCase(
            "12345",
            "user1",
            "org1",
            CaseRole.CREATOR
        );
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        AssignCaseReference ref = new AssignCaseReference();
        ref.setCaseReference("123");

        assertThrows(IllegalArgumentException.class, () ->
            assignUserWithACaseRole.migrateCaseData(null, ref)
        );
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();

        AssignCaseReference ref = new AssignCaseReference();
        ref.setCaseReference(null);

        assertThrows(IllegalArgumentException.class, () ->
            assignUserWithACaseRole.migrateCaseData(caseData, ref)
        );
    }
}
