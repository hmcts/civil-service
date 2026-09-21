package uk.gov.hmcts.reform.civil.service.search.hearingfee;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;

/**
 * Provides the ElasticSearch query for identifying cases where the hearing fee needs to be checked.
 * This identifies cases in the 'HEARING_READINESS' state.
 */
@Component
@Slf4j
public class HearingFeeDueQueryProvider implements PaginatedQueryProvider {

    private static final int START_INDEX = 0;

    private final CommonQueryConstructs commonQueryConstructs;

    public HearingFeeDueQueryProvider(CommonQueryConstructs commonQueryConstructs) {
        this.commonQueryConstructs = commonQueryConstructs;
    }

    /**
     * Builds the paginated query for cases awaiting a hearing fee check.
     *
     * @param pageToken the token containing the 'search_after' value
     * @param pageSize the number of results to return
     * @return a PaginatedQuery containing the ES query
     */
    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        log.info("Call to HearingFeeDueQueryProvider query with pageSize {}", pageSize);

        return new PaginatedQuery(
            buildHearingFeeDueQuery(),
            List.of("reference"),
            START_INDEX,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder buildHearingFeeDueQuery() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(commonQueryConstructs.beState(HEARING_READINESS))
            );
    }
}
