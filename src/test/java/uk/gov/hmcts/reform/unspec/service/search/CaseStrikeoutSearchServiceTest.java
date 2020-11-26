package uk.gov.hmcts.reform.unspec.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.unspec.model.search.Query;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class CaseStrikeoutSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new CaseStrikeoutSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .must(rangeQuery("data.applicantSolicitorSecondResponseDeadlineToRespondentSolicitor1").lt("now"));

        return new Query(query, List.of("reference"), fromValue);
    }
}
