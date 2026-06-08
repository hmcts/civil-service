package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
@RequiredArgsConstructor
public class JudgementBufferExpiredSearchService {

    private final ElasticSearchPaginatedStreamProvider elasticSearchPaginatedStreamProvider;

    public Set<CaseDetails> getCases() {
        return getCasesStream().collect(java.util.stream.Collectors.toSet());
    }

    public Stream<CaseDetails> getCasesStream() {
        String timeNow = ZonedDateTime.now(ZoneOffset.UTC).toString();

        return elasticSearchPaginatedStreamProvider.getPaginatedStream(
            searchAfterValue -> query(timeNow, searchAfterValue)
        );
    }

    private PaginatedQuery query(String timeNow, String searchAfterValue) {
        log.info("Call to JudgementBufferExpiredSearchService query with timeNow {}", timeNow);
        ZonedDateTime timeMinus48Hours = ZonedDateTime.parse(timeNow).minusHours(48);
        return new PaginatedQuery(
            generateSearchForExpiredJudgments(timeMinus48Hours),
            List.of("reference"),
            0,
            searchAfterValue == null,
            searchAfterValue,
            50,
            "id"
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
