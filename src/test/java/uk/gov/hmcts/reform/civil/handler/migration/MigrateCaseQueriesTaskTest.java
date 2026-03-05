package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class MigrateCaseQueriesTaskTest {

    private MigrateCaseQueriesTask task;

    @BeforeEach
    void setUp() {
        task = new MigrateCaseQueriesTask();
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("MigrateCaseQueriesTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(task.getEventSummary()).isEqualTo("Migrate case queries via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(task.getEventDescription()).isEqualTo("This task migrates case queries on the case");
    }

    @Test
    void shouldMigrateCaseQueriesWhenOldQueriesExist() {
        List<Element<CaseMessage>> messages = new ArrayList<>();
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId("1");
        messages.add(element(caseMessage));

        CaseQueriesCollection oldCollection = new CaseQueriesCollection();
        oldCollection.setPartyName("Old Party");
        oldCollection.setCaseMessages(messages);

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(oldCollection)
            .build();

        CaseReference caseReference = caseReference("123");

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertThat(result.getQmApplicantSolicitorQueries()).isNull();
        assertThat(result.getQueries()).isNotNull();
        assertThat(result.getQueries().getCaseMessages()).hasSize(1);
        assertThat(result.getQueries().getPartyName()).isEqualTo("All queries");
    }

    @Test
    void shouldNotMigrateWhenNoOldCaseQueriesExist() {
        CaseData caseData = CaseData.builder().build();
        CaseReference caseReference = caseReference("123");

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertThat(result.getQueries()).isNull();
        assertThat(result.getQmApplicantSolicitorQueries()).isNull();
    }

    private CaseReference caseReference(String value) {
        CaseReference reference = new CaseReference();
        reference.setCaseReference(value);
        return reference;
    }
}
