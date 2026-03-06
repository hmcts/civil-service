package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssignCaseReferenceTest {

    @Test
    void shouldPopulateFieldsFromExcelRow() throws Exception {
        AssignCaseReference assignCaseReference = new AssignCaseReference();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "12345");
        rowValues.put("userId", "user1");
        rowValues.put("organisationId", "org1");
        rowValues.put("caseRole", "CREATOR");

        assignCaseReference.fromExcelRow(rowValues);

        assertEquals("12345", assignCaseReference.getCaseReference());
        assertEquals("user1", assignCaseReference.getUserId());
        assertEquals("org1", assignCaseReference.getOrganisationId());
        assertEquals("CREATOR", assignCaseReference.getCaseRole());
    }

    @Test
    void shouldHandleNullValues() throws Exception {
        AssignCaseReference assignCaseReference = new AssignCaseReference();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", null);
        rowValues.put("userId", null);
        rowValues.put("organisationId", null);
        rowValues.put("caseRole", null);

        assignCaseReference.fromExcelRow(rowValues);

        assertNull(assignCaseReference.getCaseReference());
        assertNull(assignCaseReference.getUserId());
        assertNull(assignCaseReference.getOrganisationId());
        assertNull(assignCaseReference.getCaseRole());
    }

    @Test
    void shouldIgnoreMissingFields() throws Exception {
        AssignCaseReference assignCaseReference = new AssignCaseReference();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "99999");

        assignCaseReference.fromExcelRow(rowValues);

        assertEquals("99999", assignCaseReference.getCaseReference());
        assertNull(assignCaseReference.getUserId());
        assertNull(assignCaseReference.getOrganisationId());
        assertNull(assignCaseReference.getCaseRole());
    }
}
