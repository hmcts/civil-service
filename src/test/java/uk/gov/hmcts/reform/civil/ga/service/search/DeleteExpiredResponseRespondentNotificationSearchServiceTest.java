package uk.gov.hmcts.reform.civil.ga.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ExtendWith(SpringExtension.class)
public class DeleteExpiredResponseRespondentNotificationSearchServiceTest {

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    @Captor
    protected ArgumentCaptor<Query> queryCaptor;

    @Mock
    protected GaCoreCaseDataService coreCaseDataService;

    protected DeleteExpiredResponseRespondentNotificationSearchService searchService;

    @BeforeEach
    void setup() {
        searchService = new DeleteExpiredResponseRespondentNotificationSearchService(coreCaseDataService);
    }

    @Test
    void shouldCallGetCasesOnce_WhenNoCasesReturned() {
        SearchResult searchResult = buildSearchResult(0, emptyList());

        when(coreCaseDataService.searchGeneralApplication(any())).thenReturn(searchResult);

        assertThat(searchService.getApplications()).isEmpty();
        verify(coreCaseDataService).searchGeneralApplication(queryCaptor.capture());
        assertThat(queryCaptor.getValue()).usingRecursiveComparison()
            .isEqualTo(query(0));
    }

    private Query query(int startIndex) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalDateTime.now().toLocalTime();
        LocalDate targetDate = currentTime.isAfter(END_OF_BUSINESS_DAY) ? currentDate.plusDays(1) : currentDate;
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.judicialDecisionRequestMoreInfo.judgeRequestMoreInfoByDate").lt(targetDate
                                                                                                                       .toString()))
                            .mustNot(matchQuery("data.respondentResponseDeadlineChecked", "Yes"))
                            .must(beState(AWAITING_RESPONDENT_RESPONSE))),
            List.of("reference"),
            startIndex
        );
    }

    private SearchResult buildSearchResult(int i, List<CaseDetails> caseDetails) {
        return SearchResult.builder()
            .total(i)
            .cases(caseDetails)
            .build();
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
