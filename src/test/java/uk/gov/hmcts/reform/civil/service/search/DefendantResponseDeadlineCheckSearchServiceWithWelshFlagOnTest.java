package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.when;

class DefendantResponseDeadlineCheckSearchServiceWithWelshFlagOnTest extends ElasticSearchServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        searchService = new DefendantResponseDeadlineCheckSearchService(coreCaseDataService, featureToggleService);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.respondent1ResponseDeadline").lt("now"))
                        .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT")))
                        .mustNot(existsQuery("data.respondent1ResponseDate"))
                        .must(boolQuery()
                                  .minimumShouldMatch(1)
                                  .should(boolQuery().mustNot(existsQuery("data.businessProcess")))
                                  .should(boolQuery().mustNot(existsQuery("data.businessProcess.status")))
                                  .should(boolQuery().must(matchQuery("data.businessProcess.status", "FINISHED"))))
            );

        return new Query(query, List.of("reference"), fromValue);
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {
        return null;
    }
}
