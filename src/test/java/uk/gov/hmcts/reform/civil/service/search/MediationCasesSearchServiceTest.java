package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

public class MediationCasesSearchServiceTest extends ElasticSearchServiceTest {

    private static final LocalDateTime CARM_DATE = LocalDateTime.of(2024, 11, 5,
                                                                    7, 28, 35);

    @BeforeEach
    void setup() {
        searchService = new MediationCasesSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        return null;
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date, boolean carmEnabled,
                                          boolean initialSearch,
                                          String searchAfterValue) {
        String targetDateString =
            date.format(DateTimeFormatter.ISO_DATE);
        if (carmEnabled) {
            BoolQueryBuilder query = boolQuery()
                .must(matchAllQuery())
                .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                .must(boolQuery().must(rangeQuery("data.submittedDate").gte(CARM_DATE)))
                .must(rangeQuery("data.claimMovedToMediationOn").gte(targetDateString))
                .mustNot(matchQuery("data.mediationFileSentToMmt", "Yes"));
            return new Query(query, Collections.emptyList(), fromValue, initialSearch, searchAfterValue);
        } else {
            BoolQueryBuilder query = boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(boolQuery().must(matchQuery("state", "IN_MEDIATION")))
                            .must(boolQuery().must(rangeQuery("data.submittedDate").lt(CARM_DATE)))
                            .must(rangeQuery("data.claimMovedToMediationOn").gte(targetDateString)))
                .mustNot(matchQuery("data.mediationFileSentToMmt", "Yes"));

            return new Query(query, Collections.emptyList(), fromValue);
        }
    }
}
