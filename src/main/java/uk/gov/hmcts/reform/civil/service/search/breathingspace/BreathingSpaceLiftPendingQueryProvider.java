package uk.gov.hmcts.reform.civil.service.search.breathingspace;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Component
@Slf4j
public class BreathingSpaceLiftPendingQueryProvider implements PaginatedQueryProvider {

    private static final int START_INDEX = 0;

    private final Time time;

    public BreathingSpaceLiftPendingQueryProvider(Time time) {
        this.time = time;
    }

    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        String today = time.now().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("Call to BreathingSpaceLiftPendingQueryProvider query with today {}", today);

        return new PaginatedQuery(
            buildDueLiftQuery(today),
            List.of("reference"),
            START_INDEX,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder buildDueLiftQuery(String today) {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .must(matchQuery("data.breathingSpaceActive", "Yes"))
                        .must(matchQuery("data.breathingSpaceLiftPending", "Yes"))
                        .must(rangeQuery("data.liftBreathing.expectedEnd").lte(today))
                        .must(haveNoOngoingBusinessProcess()));
    }

    private BoolQueryBuilder haveNoOngoingBusinessProcess() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery().mustNot(existsQuery("data.businessProcess")))
            .should(boolQuery().mustNot(existsQuery("data.businessProcess.status")))
            .should(boolQuery().must(matchQuery("data.businessProcess.status", "FINISHED")));
    }
}
