package uk.gov.hmcts.reform.civil.ga.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

public class CaseStateSearchServiceTest extends GaElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseStateSearchService(coreCaseDataService);
    }

    @ParameterizedTest
    @EnumSource(value = CaseState.class, names = {
        "AWAITING_RESPONDENT_RESPONSE", "APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION"
    })
    void shouldExcludeCheckedResponseDeadlinesOnEveryPage(CaseState caseState) throws Exception {
        when(coreCaseDataService.searchGeneralApplication(any()))
            .thenReturn(buildSearchResultWithTotalCases(11));

        ((CaseStateSearchService) searchService).getUncheckedResponseDeadlineApplications(caseState);

        verify(coreCaseDataService, times(2)).searchGeneralApplication(queryCaptor.capture());
        for (int page = 0; page < 2; page++) {
            var query = ObjectMapperFactory.instance().readTree(queryCaptor.getAllValues().get(page).toString());
            assertThat(query.at("/query/bool/must/0/match/state/query").asText()).isEqualTo(caseState.toString());
            assertThat(query.at("/query/bool/must_not/0/match/data.respondentResponseDeadlineChecked/query").asText())
                .isEqualTo("Yes");
            assertThat(query.get("from").asInt()).isEqualTo(page * 10);
        }
    }

    @Override
    protected Query buildQuery(int fromValue, CaseState caseState) {
        return new Query(
            matchQuery("state", caseState.toString()),
            emptyList(),
            fromValue
        );
    }

    @Override
    protected Query queryForOrderMade(int startIndex, CaseState caseState, GeneralApplicationTypes gaType) {
        MatchQueryBuilder queryCaseState = QueryBuilders.matchQuery("state", caseState.toString());
        MatchQueryBuilder queryGaType = QueryBuilders.matchQuery("data.generalAppType.types", gaType);
        MatchQueryBuilder consentOrder = QueryBuilders
            .matchQuery("data.approveConsentOrder.isOrderProcessedByStayScheduler", "No");
        MatchQueryBuilder queryOrderProcessStatus = gaType.equals(UNLESS_ORDER)
            ? QueryBuilders
            .matchQuery("data.judicialDecisionMakeOrder.isOrderProcessedByUnlessScheduler", "No")
            : QueryBuilders
            .matchQuery("data.judicialDecisionMakeOrder.isOrderProcessedByStayScheduler", "No");

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(queryCaseState)
            .must(queryGaType)
            .should(queryOrderProcessStatus)
            .should(consentOrder)
            .minimumShouldMatch(1);

        return new Query(
            query,
            emptyList(),
            startIndex
        );
    }
}
