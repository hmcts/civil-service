package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

/**
 * Search service for retrieving cases that have a hearing date in 10 business days from current date (Includes bank holidays).
 */
@Service
@Slf4j
public class CaseHearingDateSearchService extends ElasticSearchService {

    private static final int BUSINESS_DAYS_FROM_NOW_MAX = 10;
    private static final int BUSINESS_DAYS_FROM_NOW_MIN = 3;

    public CaseHearingDateSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to CaseDismissedSearchService query with index {} and timeNow {}", startIndex, timeNow);
        LocalDate baseDate = ZonedDateTime.parse(timeNow).toLocalDate();
        String targetMaxDateString = DateUtils.addDaysSkippingWeekends(
            baseDate, BUSINESS_DAYS_FROM_NOW_MAX).format(DateTimeFormatter.ISO_DATE);
        String targetMinDateString = DateUtils.addDaysSkippingWeekends(
            baseDate, BUSINESS_DAYS_FROM_NOW_MIN).format(DateTimeFormatter.ISO_DATE);
        return new Query(
            boolQuery()
                .mustNot(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING"))
                .must(boolQuery()
                          .minimumShouldMatch(1)
                          .should(rangeQuery("data.hearingDate").lte(targetMaxDateString).gte(
                              targetMinDateString))
                ),
            List.of("reference"),
            startIndex
        );
    }
}
