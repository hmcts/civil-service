package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class CaseStateSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseStateSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        return null;
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date) {
        String targetDateString =
            date.format(DateTimeFormatter.ISO_DATE);
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                        .must(matchQuery("data.claimMovedToMediationOn", targetDateString)));

        return new Query(query, Collections.emptyList(), fromValue);
    }
}
