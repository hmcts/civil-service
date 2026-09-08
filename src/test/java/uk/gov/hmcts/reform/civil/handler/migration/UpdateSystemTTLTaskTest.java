package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UpdateTTLCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.cmc.model.TTL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateSystemTTLTaskTest {

    private static final String CASE_REFERENCE = "1234567890123456";

    private UpdateSystemTTLTask task;

    @BeforeEach
    void setUp() {
        task = new UpdateSystemTTLTask();
    }

    @Test
    void shouldExposeTaskMetadata() {
        assertThat(task.getType()).isEqualTo(UpdateTTLCaseReference.class);
        assertThat(task.getTaskName()).isEqualTo("UpdateSystemTTLTask");
        assertThat(task.getEventSummary()).isEqualTo("Update case system TTL via migration task");
        assertThat(task.getEventDescription()).isEqualTo("This task updates system TTL on the case");
    }

    @Test
    void shouldLeaveCivilCaseDataUnchanged() {
        CaseData caseData = CaseData.builder().build();

        CaseData result = task.migrateCaseData(caseData, caseReference(null));

        assertThat(result).isSameAs(caseData);
    }

    @Test
    void shouldAddSystemTtlToCmcCaseWhenSpreadsheetTtlIsMissing() {
        Map<String, Object> originalData = new HashMap<>(Map.of("existing", "value"));
        CaseDetails caseDetails = CaseDetails.builder()
            .data(originalData)
            .lastModified(LocalDateTime.of(2020, 2, 29, 15, 30))
            .build();

        Map<String, Object> result = task.migrateCmcCaseData(caseDetails, caseReference(null));

        assertThat(result).containsEntry("existing", "value");
        assertThat(result.get("TTL"))
            .isInstanceOfSatisfying(TTL.class, ttl -> {
                assertThat(ttl.getSystemTTL()).isEqualTo(LocalDate.of(2026, 2, 28));
                assertThat(ttl.getOverrideTTL()).isNull();
                assertThat(ttl.getSuspended()).isEqualTo("No");
            });
        assertThat(originalData).doesNotContainKey("TTL");
    }

    @Test
    void shouldNotReplaceTtlWhenSpreadsheetShowsItAlreadyExists() {
        Object existingTtl = Map.of("SystemTTL", "2030-01-01");
        Map<String, Object> originalData = Map.of("TTL", existingTtl);
        CaseDetails caseDetails = CaseDetails.builder().data(originalData).build();

        Map<String, Object> result = task.migrateCmcCaseData(
            caseDetails,
            caseReference("2030-01-01")
        );

        assertThat(result).containsEntry("TTL", existingTtl);
        assertThat(result).isNotSameAs(originalData);
    }

    @Test
    void shouldRejectMissingLastModifiedDateWhenTtlNeedsCreating() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();

        assertThatThrownBy(() -> task.migrateCmcCaseData(caseDetails, caseReference(null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Last modified date must not be null when system TTL is missing");
    }

    @Test
    void shouldRejectInvalidCivilMigrationInputs() {
        assertThatThrownBy(() -> task.migrateCaseData(null, caseReference(null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), new UpdateTTLCaseReference()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
    }

    @Test
    void shouldRejectInvalidCmcMigrationInputs() {
        UpdateTTLCaseReference reference = caseReference(null);

        assertThatThrownBy(() -> task.migrateCmcCaseData(null, reference))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
        assertThatThrownBy(() -> task.migrateCmcCaseData(CaseDetails.builder().build(), reference))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
        assertThatThrownBy(() -> task.migrateCmcCaseData(caseDetails(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
        assertThatThrownBy(() -> task.migrateCmcCaseData(caseDetails(), new UpdateTTLCaseReference()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CaseData and CaseReference fields must not be null");
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder().data(Map.of()).lastModified(LocalDateTime.now()).build();
    }

    private UpdateTTLCaseReference caseReference(String systemTtl) {
        UpdateTTLCaseReference reference = new UpdateTTLCaseReference();
        reference.setCaseReference(CASE_REFERENCE);
        reference.setSystemTTL(systemTtl);
        return reference;
    }
}
