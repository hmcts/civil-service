package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UnAssignCaseReferenceTest {

    @Test
    void shouldPopulateFieldsFromExcelRow() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "12345");
        rowValues.put("userId", "user1");
        rowValues.put("organisationId", "org1");
        rowValues.put("caseRole", "CREATOR");

        UnAssignCaseReference unassignCaseReference = new UnAssignCaseReference();
        unassignCaseReference.fromExcelRow(rowValues);

        assertEquals("12345", unassignCaseReference.getCaseReference());
        assertEquals("user1", unassignCaseReference.getUserId());
        assertEquals("org1", unassignCaseReference.getOrganisationId());
        assertEquals("CREATOR", unassignCaseReference.getCaseRole());
    }

    @Test
    void shouldHandleNullValues() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", null);
        rowValues.put("userId", null);
        rowValues.put("organisationId", null);
        rowValues.put("caseRole", null);

        UnAssignCaseReference unassignCaseReference = new UnAssignCaseReference();
        unassignCaseReference.fromExcelRow(rowValues);

        assertNull(unassignCaseReference.getCaseReference());
        assertNull(unassignCaseReference.getUserId());
        assertNull(unassignCaseReference.getOrganisationId());
        assertNull(unassignCaseReference.getCaseRole());
    }

    @Test
    void shouldIgnoreMissingFields() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "99999");

        AssignCaseReference assignCaseReference = new AssignCaseReference();
        assignCaseReference.fromExcelRow(rowValues);

        assertEquals("99999", assignCaseReference.getCaseReference());
        assertNull(assignCaseReference.getUserId());
        assertNull(assignCaseReference.getOrganisationId());
        assertNull(assignCaseReference.getCaseRole());
    }
}
