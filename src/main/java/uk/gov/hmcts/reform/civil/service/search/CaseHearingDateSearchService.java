package uk.gov.hmcts.reform.civil.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;


/**
 * Search service for retrieving cases that have a hearing date in 10 business days from current date (Includes bank holidays).
 */
@Service
public class CaseHearingDateSearchService extends ElasticSearchService {

    private static final int BUSINESS_DAYS_FROM_NOW_MAX = 10;
    private static final int BUSINESS_DAYS_FROM_NOW_MIN = 3;

    public CaseHearingDateSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        String targetMaxDateString = DateUtils.addDaysSkippingWeekends(
            LocalDate.now(), BUSINESS_DAYS_FROM_NOW_MAX).format(DateTimeFormatter.ISO_DATE);
        String targetMinDateString = DateUtils.addDaysSkippingWeekends(
            LocalDate.now(), BUSINESS_DAYS_FROM_NOW_MIN).format(DateTimeFormatter.ISO_DATE);
        return new Query(
            boolQuery()
                .mustNot(matchQuery("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING"))
                .mustNot(matchQuery("data.cvpLinkSchedulerProcessed", "Yes"))
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
