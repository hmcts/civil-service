package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseRoleCaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

class UnAssignUserWithACaseRoleTest {

    @Mock
    private CoreCaseUserService coreCaseUserService;

    private UnAssignUserWithACaseRole unassignUserWithACaseRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unassignUserWithACaseRole = new UnAssignUserWithACaseRole(coreCaseUserService);
    }

    @Test
    void shouldAssignCaseRoleSuccessfully() {
        CaseRoleCaseReference unassignCaseReference = new CaseRoleCaseReference();
        unassignCaseReference.setCaseReference("12345");
        unassignCaseReference.setUserId("user1");
        unassignCaseReference.setOrganisationId("org1");
        unassignCaseReference.setCaseRole("CREATOR");

        CaseData caseData = CaseData.builder().build();
        unassignUserWithACaseRole.migrateCaseData(caseData, unassignCaseReference);

        verify(coreCaseUserService).unassignCase(
            "12345",
            "user1",
            "org1",
            CaseRole.CREATOR
        );
    }

    @Test
    void shouldThrowExceptionWhenCaseDataIsNull() {
        CaseRoleCaseReference ref = new CaseRoleCaseReference();
        ref.setCaseReference("123");

        assertThrows(IllegalArgumentException.class, () ->
            unassignUserWithACaseRole.migrateCaseData(null, ref)
        );
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();

        CaseRoleCaseReference ref = new CaseRoleCaseReference();
        ref.setCaseReference(null);

        assertThrows(IllegalArgumentException.class, () ->
            unassignUserWithACaseRole.migrateCaseData(caseData, ref)
        );
    }
}
