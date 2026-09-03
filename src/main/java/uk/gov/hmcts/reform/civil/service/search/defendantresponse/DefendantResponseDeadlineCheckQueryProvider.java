package uk.gov.hmcts.reform.civil.service.search.defendantresponse;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.time.ZoneOffset;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

/**
 * Provides the ElasticSearch query for identifying cases where the defendant response deadline has passed.
 * This identifies cases in 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT' state whose response deadline is in the
 * past, that have not already been checked and have no ongoing business process.
 */
@Component
@Slf4j
public class DefendantResponseDeadlineCheckQueryProvider implements PaginatedQueryProvider {

    private static final int START_INDEX = 0;

    private final CommonQueryConstructs commonQueryConstructs;
    private final FeatureToggleService featureToggleService;
    private final Time time;

    public DefendantResponseDeadlineCheckQueryProvider(CommonQueryConstructs commonQueryConstructs,
                                                       FeatureToggleService featureToggleService,
                                                       Time time) {
        this.commonQueryConstructs = commonQueryConstructs;
        this.featureToggleService = featureToggleService;
        this.time = time;
    }

    /**
     * Builds the paginated query for expired defendant response deadlines.
     *
     * @param pageToken the token containing the 'search_after' value
     * @param pageSize the number of results to return
     * @return a PaginatedQuery containing the ES query
     */
    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        String timeNow = time.now().atZone(LOCAL_ZONE).withZoneSameInstant(ZoneOffset.UTC).toString();
        log.info("Call to DefendantResponseDeadlineCheckQueryProvider query with timeNow {}", timeNow);

        return new PaginatedQuery(
            buildDeadlineExpiredQuery(timeNow),
            List.of("reference"),
            START_INDEX,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder buildDeadlineExpiredQuery(String timeNow) {
        BoolQueryBuilder deadlineExpired = boolQuery()
            .must(rangeQuery("data.respondent1ResponseDeadline").lt(timeNow))
            .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
            .must(commonQueryConstructs.beState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
            .must(commonQueryConstructs.haveNoOngoingBusinessProcess());

        if (featureToggleService.isWelshEnabledForMainCase()) {
            deadlineExpired.mustNot(existsQuery("data.respondent1ResponseDate"));
        }

        return boolQuery()
            .minimumShouldMatch(1)
            .should(deadlineExpired);
    }
}
