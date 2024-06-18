package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.config.CarmDateConfiguration;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

public class MediationCasesSearchServiceTest extends ElasticSearchServiceTest {

    @Mock
    protected CarmDateConfiguration carmDateConfiguration;

    @BeforeEach
    void setup() {
        searchService = new MediationCasesSearchService(coreCaseDataService, carmDateConfiguration);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        return null;
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled) {
        String targetDateString =
            date.format(DateTimeFormatter.ISO_DATE);
        if (carmEnabled) {
            BoolQueryBuilder query = boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                            .must(boolQuery().must(rangeQuery("data.submittedDate").gte(carmDateConfiguration.getCarmDate())))
                            .must(matchQuery("data.claimMovedToMediationOn", targetDateString)));
            return new Query(query, Collections.emptyList(), fromValue);
        } else {
            BoolQueryBuilder query = boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                            .must(boolQuery().must(rangeQuery("data.submittedDate").lt(carmDateConfiguration.getCarmDate())))
                            .must(matchQuery("data.claimMovedToMediationOn", targetDateString)));

            return new Query(query, Collections.emptyList(), fromValue);
        }
    }
}
