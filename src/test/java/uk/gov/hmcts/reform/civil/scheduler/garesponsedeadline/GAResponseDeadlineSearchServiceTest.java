package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ExtendWith(MockitoExtension.class)
class GAResponseDeadlineSearchServiceTest {

    @Mock
    private CaseStateSearchService searchService;

    @Mock
    private GAResponseDeadlineFilter filter;

    @InjectMocks
    private GAResponseDeadlineSearchService searchServiceToTest;

    @Test
    void shouldReturnCasesWhenDeadlineIsExpiredForBothAwaitingResponseAndUrgentApplications() {
        CaseDetails awaitingExpiredCase = CaseDetails.builder().id(1L).build();
        CaseDetails awaitingFutureCase = CaseDetails.builder().id(2L).build();
        CaseDetails urgentExpiredCase = CaseDetails.builder().id(3L).build();
        CaseDetails urgentFutureCase = CaseDetails.builder().id(4L).build();

        when(searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of(awaitingExpiredCase, awaitingFutureCase));
        when(searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
            .thenReturn(Set.of(urgentExpiredCase, urgentFutureCase));

        when(filter.isDeadlineExpired(awaitingExpiredCase)).thenReturn(true);
        when(filter.isDeadlineExpired(awaitingFutureCase)).thenReturn(false);
        when(filter.isDeadlineExpired(urgentExpiredCase)).thenReturn(true);
        when(filter.isDeadlineExpired(urgentFutureCase)).thenReturn(false);

        TaskResult<CaseDetails> result = searchServiceToTest.getElasticSearchResult();

        assertThat(result).isInstanceOf(ListTaskResult.class);
        assertThat(result.totalResults()).isEqualTo(2);
        assertThat(result.itemStream()).containsExactlyInAnyOrder(awaitingExpiredCase, urgentExpiredCase);

        verify(searchService).getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);
        verify(searchService).getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldReturnEmptyWhenNoCasesFound() {
        when(searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of());
        when(searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
            .thenReturn(Set.of());

        TaskResult<CaseDetails> result = searchServiceToTest.getElasticSearchResult();

        assertThat(result).isInstanceOf(ListTaskResult.class);
        assertThat(result.totalResults()).isZero();
        assertThat(result.itemStream()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCasesFoundAreNotExpired() {
        CaseDetails awaitingFutureCase = CaseDetails.builder().id(1L).build();
        CaseDetails urgentFutureCase = CaseDetails.builder().id(2L).build();

        when(searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of(awaitingFutureCase));
        when(searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
            .thenReturn(Set.of(urgentFutureCase));

        when(filter.isDeadlineExpired(awaitingFutureCase)).thenReturn(false);
        when(filter.isDeadlineExpired(urgentFutureCase)).thenReturn(false);

        TaskResult<CaseDetails> result = searchServiceToTest.getElasticSearchResult();

        assertThat(result.totalResults()).isZero();
        assertThat(result.itemStream()).isEmpty();
    }

    @Test
    void shouldReturnOnlyAwaitingResponseCasesWhenUrgentApplicationsIsEmpty() {
        CaseDetails awaitingExpiredCase = CaseDetails.builder().id(1L).build();

        when(searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of(awaitingExpiredCase));
        when(searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
            .thenReturn(Set.of());

        when(filter.isDeadlineExpired(awaitingExpiredCase)).thenReturn(true);

        TaskResult<CaseDetails> result = searchServiceToTest.getElasticSearchResult();

        assertThat(result.totalResults()).isEqualTo(1);
        assertThat(result.itemStream()).containsExactly(awaitingExpiredCase);
    }

    @Test
    void shouldReturnOnlyUrgentApplicationsWhenAwaitingResponseIsEmpty() {
        CaseDetails urgentExpiredCase = CaseDetails.builder().id(2L).build();

        when(searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of());
        when(searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
            .thenReturn(Set.of(urgentExpiredCase));

        when(filter.isDeadlineExpired(urgentExpiredCase)).thenReturn(true);

        TaskResult<CaseDetails> result = searchServiceToTest.getElasticSearchResult();

        assertThat(result.totalResults()).isEqualTo(1);
        assertThat(result.itemStream()).containsExactly(urgentExpiredCase);
    }
}
