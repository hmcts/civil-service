package uk.gov.hmcts.reform.civil.service.search.judgementbuffer;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.calculator.SearchDateTimeCalculator;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

/**
 * Provides the ElasticSearch query for identifying expired judgements.
 * This identifies cases in 'JUDGMENT_REQUESTED' state that have been in that state for more than 48 hours
 * and have no ongoing business process.
 */
@Component
@Slf4j
public class JudgementBufferExpiredQueryProvider implements PaginatedQueryProvider {

    private static final long JUDGEMENT_BUFFER_WORKING_HOURS = 48L;
    private static final int START_INDEX = 0;
    private final CommonQueryConstructs commonQueryConstructs;
    private final SearchDateTimeCalculator dateTimeCalculator;
    private final Time time;

    public JudgementBufferExpiredQueryProvider(CommonQueryConstructs commonQueryConstructs,
                                               SearchDateTimeCalculator dateTimeCalculator,
                                               Time time) {
        this.commonQueryConstructs = commonQueryConstructs;
        this.dateTimeCalculator = dateTimeCalculator;
        this.time = time;
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
        ZonedDateTime zonedDateTime = time.now().atZone(LOCAL_ZONE);
        log.info("Call to JudgementBufferExpiredQueryProvider query with timeNow {}", zonedDateTime);

        ZonedDateTime timeMinus48WorkingHours = dateTimeCalculator.minusWorkingHours(zonedDateTime, JUDGEMENT_BUFFER_WORKING_HOURS);
        return new PaginatedQuery(
            buildExpiredJudgmentsQuery(timeMinus48WorkingHours),
            List.of("reference"),
            START_INDEX,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder buildExpiredJudgmentsQuery(ZonedDateTime timeMinus48WorkingHours) {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(rangeQuery("data.joDJCreatedDate").lte(formatToIsoLocal(timeMinus48WorkingHours)))
                        .must(commonQueryConstructs.beState(CaseState.JUDGMENT_REQUESTED))
                        .must(commonQueryConstructs.haveNoOngoingBusinessProcess())
            );
    }

    private String formatToIsoLocal(ZonedDateTime timeMinus48WorkingHours) {
        return timeMinus48WorkingHours.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
