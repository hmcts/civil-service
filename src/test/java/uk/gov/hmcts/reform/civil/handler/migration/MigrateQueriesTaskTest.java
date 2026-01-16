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
class MigrateQueriesTaskTest {

    private MigrateQueriesTask task;

    @BeforeEach
    void setUp() {
        task = new MigrateQueriesTask();
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("MigrateQueriesTask");
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(task.getEventSummary()).isEqualTo("Migrate queries via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(task.getEventDescription()).isEqualTo("This task migrates queries on the case");
    }

    @Test
    void shouldMigrateQueriesWhenOldQueriesExist() {
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

        CaseReference caseReference = CaseReference.builder().caseReference("123").build();

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertThat(result.getQmApplicantSolicitorQueries()).isNotNull();
        assertThat(result.getQueries()).isNotNull();
        assertThat(result.getQueries().getCaseMessages()).hasSize(1);
        assertThat(result.getQueries().getPartyName()).isEqualTo("All queries");
    }

    @Test
    void shouldNotMigrateWhenNoOldQueriesExist() {
        CaseData caseData = CaseData.builder().build();
        CaseReference caseReference = CaseReference.builder().caseReference("123").build();

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertThat(result.getQueries()).isNull();
        assertThat(result.getQmApplicantSolicitorQueries()).isNull();
    }
}
