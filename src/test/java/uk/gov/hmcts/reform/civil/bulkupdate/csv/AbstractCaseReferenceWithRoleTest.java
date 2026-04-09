package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AbstractCaseReferenceWithRoleTest {

    public static class DummyData {
        public String name;
    }

    // Concrete test subclass
    static class DummyCaseReference extends AbstractCaseReferenceWithRole<DummyData> {
        @Override
        protected Class<DummyData> getObjectType() {
            return DummyData.class;
        }
    }

    private DummyCaseReference caseReference;

    @BeforeEach
    void setUp() {
        caseReference = new DummyCaseReference();
    }

    @Test
    void shouldSetCaseReferenceFromRowValues() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "CASE-999");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("CASE-999");
    }

    @Test
    void shouldMapApplicant1Correctly() throws Exception {
        DummyData data = new DummyData();
        data.name = "Applicant 1";

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("applicant1", data);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject().name).isEqualTo("Applicant 1");
        assertThat(caseReference.isApplicant1()).isTrue();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapApplicant2Correctly() throws Exception {
        DummyData data = new DummyData();
        data.name = "Applicant 2";

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("applicant2", data);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject().name).isEqualTo("Applicant 2");
        assertThat(caseReference.isApplicant2()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapRespondent1Correctly() throws Exception {
        DummyData data = new DummyData();
        data.name = "Respondent 1";

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("respondent1", data);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject().name).isEqualTo("Respondent 1");
        assertThat(caseReference.isRespondent1()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapRespondent2Correctly() throws Exception {
        DummyData data = new DummyData();
        data.name = "Respondent 2";

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("respondent2", data);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject().name).isEqualTo("Respondent 2");
        assertThat(caseReference.isRespondent2()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
    }

    @Test
    void shouldIgnoreUnknownKeysAndNotThrow() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("randomField", "someValue");
        rowValues.put("caseReference", "CASE-101");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("CASE-101");
        assertThat(caseReference.getDataObject()).isNull();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }
}
