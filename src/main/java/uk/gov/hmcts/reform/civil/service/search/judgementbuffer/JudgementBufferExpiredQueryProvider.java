package uk.gov.hmcts.reform.civil.service.search.judgementbuffer;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

/**
 * Provides the ElasticSearch query for identifying expired judgements.
 * This identifies cases in 'JUDGMENT_REQUESTED' state that have been in that state for more than 48 hours
 * and have no ongoing business process.
 */
@Component
@Slf4j
public class JudgementBufferExpiredQueryProvider implements PaginatedQueryProvider {

    private final CommonQueryConstructs commonQueryConstructs;

    public JudgementBufferExpiredQueryProvider(CommonQueryConstructs commonQueryConstructs) {
        this.commonQueryConstructs = commonQueryConstructs;
    }

    /**
     * Builds the paginated query for expired judgements.
     *
     * @param pageToken the token containing the 'search_after' value
     * @param pageSize the number of results to return
     * @return a PaginatedQuery containing the ES query
     */
    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        ZonedDateTime timeNow = ZonedDateTime.now(ZoneOffset.UTC);
        log.info("Call to JudgementBufferExpiredSearchService query with timeNow {}", timeNow);
        ZonedDateTime timeMinus48Hours = timeNow.minusHours(48);
        return new PaginatedQuery(
            generateSearchForExpiredJudgments(timeMinus48Hours),
            List.of("reference"),
            0,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder generateSearchForExpiredJudgments(ZonedDateTime timeMinus48Hours) {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.joDJCreatedDate").lte(timeMinus48Hours))
                        .must(commonQueryConstructs.beState(CaseState.JUDGMENT_REQUESTED))
                        .must(commonQueryConstructs.haveNoOngoingBusinessProcess())
            );
    }
}
