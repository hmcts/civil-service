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
        assertThat(task.getEventSummary()).isEqualTo("Migrate queries");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(task.getEventDescription()).isEqualTo("Migrate queries");
    }

    @Test
    void shouldMigrateQueriesWhenOldQueriesExist() {
        List<Element<CaseMessage>> messages = new ArrayList<>();
        messages.add(element(CaseMessage.builder().id("1").build()));

        CaseQueriesCollection oldCollection = CaseQueriesCollection.builder()
            .caseMessages(messages)
            .partyName("Old Party")
            .build();

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(oldCollection)
            .build();

        CaseReference caseReference = CaseReference.builder().caseReference("123").build();

        CaseData result = task.migrateCaseData(caseData, caseReference);

        assertThat(result.getQmApplicantSolicitorQueries()).isNull();
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
