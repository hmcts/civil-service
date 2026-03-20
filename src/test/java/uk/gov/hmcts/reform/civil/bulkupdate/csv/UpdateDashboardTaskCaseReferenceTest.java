package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateDashboardTaskCaseReferenceTest {

    @Test
    void shouldPopulateFieldsFromExcelRow() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", 12345L);
        rowValues.put("taskItemTemplateId", "3fa85f64-5717-4562-b3fc-2c963f66afa6");
        rowValues.put("currentStatus", 1);
        rowValues.put("nextStatus", 2);

        UpdateDashboardTaskCaseReference caseReference = new UpdateDashboardTaskCaseReference();
        caseReference.fromExcelRow(rowValues);

        assertEquals("12345", caseReference.getCaseReference());
        assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", caseReference.getTaskItemTemplateId());
        assertEquals("1", caseReference.getCurrentStatus());
        assertEquals("2", caseReference.getNextStatus());
    }

    @Test
    void shouldHandleMissingAndNullValues() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "12345");
        rowValues.put("taskItemTemplateId", null);

        UpdateDashboardTaskCaseReference caseReference = new UpdateDashboardTaskCaseReference();
        caseReference.fromExcelRow(rowValues);

        assertEquals("12345", caseReference.getCaseReference());
        assertNull(caseReference.getTaskItemTemplateId());
        assertNull(caseReference.getCurrentStatus());
        assertNull(caseReference.getNextStatus());
    }
}
