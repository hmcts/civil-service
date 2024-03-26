package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

class RespondentResponseDeadlineCheckSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new RespondentResponseDeadlineCheckSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                .must(rangeQuery("data.respondent1ResponseDeadline").lt(LocalDate.now().atTime(LocalTime.MIN)
                                                            .toString()))
                .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
                .must(boolQuery().must(matchQuery("state", "AWAITING_DEFENDANT_RESPONSE"))));

        return new Query(query, List.of("reference"), fromValue);
    }

    @Override
    protected Query buildQueryInMediation(int fromValue, LocalDate date) {
        return null;
    }
}
