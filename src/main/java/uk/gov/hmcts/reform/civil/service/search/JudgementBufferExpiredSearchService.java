package uk.gov.hmcts.reform.civil.service.search;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
@AllArgsConstructor
public class JudgementBufferExpiredSearchService {

    protected static final int START_INDEX = 0;

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;

    public Stream<CaseDetails> getCases() {
        String timeNow = ZonedDateTime.now(ZoneOffset.UTC).toString();

        return elasticSearchPaginatedStreamProvider.getPaginatedStream(searchAfterValue -> query(START_INDEX, timeNow, searchAfterValue));
    }

    private PaginatedQuery query(int startIndex, String timeNow, String searchAfterValue) {
        log.info("Call to JudgementBufferExpiredSearchService query with index {} and timeNow {}", startIndex, timeNow);
        ZonedDateTime timeMinus48Hours = ZonedDateTime.parse(timeNow).minusHours(48);
        return new PaginatedQuery(
            generateSearchForExpiredJudgments(timeMinus48Hours),
            List.of("reference"),
            startIndex,
            startIndex == 0 && searchAfterValue == null,
            searchAfterValue
        );
    }

    private BoolQueryBuilder generateSearchForExpiredJudgments(ZonedDateTime timeMinus48Hours) {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.joDJCreatedDate").lte(timeMinus48Hours))
                        .must(beState(CaseState.JUDGMENT_REQUESTED))
                        .must(haveNoOngoingBusinessProcess())
            );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }

    public BoolQueryBuilder haveNoOngoingBusinessProcess() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery().mustNot(existsQuery("data.businessProcess")))
            .should(boolQuery().mustNot(existsQuery("data.businessProcess.status")))
            .should(boolQuery().must(matchQuery("data.businessProcess.status", "FINISHED")));
    }
}
