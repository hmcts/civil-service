package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;

@Service
@Slf4j
public class RequestForReconsiderationNotificationDeadlineSearchService extends ElasticSearchService {

    private final CommonQueryConstructs commonQueryConstructs;

    public RequestForReconsiderationNotificationDeadlineSearchService(CoreCaseDataService coreCaseDataService,
                                                                      CommonQueryConstructs commonQueryConstructs) {
        super(coreCaseDataService);
        this.commonQueryConstructs = commonQueryConstructs;
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to RequestForReconsiderationNotificationDeadlineSearchService query with index {} and timeNow {}", startIndex, timeNow);
        String deadlineCutoff = getDeadlineCutoff(timeNow);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.requestForReconsiderationDeadline").lt(deadlineCutoff))
                            .mustNot(matchQuery("data.requestForReconsiderationDeadlineChecked", "Yes"))
                            .must(commonQueryConstructs.beState(CASE_PROGRESSION, HEARING_READINESS))),
            List.of("reference"),
            startIndex
        );
    }

    private String getDeadlineCutoff(String timeNow) {
        return ZonedDateTime.parse(timeNow)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDate()
            .atTime(LocalTime.MIN)
            .toString();
    }
}
