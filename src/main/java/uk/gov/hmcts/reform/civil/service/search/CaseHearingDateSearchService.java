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


/**
 * Search service for retrieving cases that have a hearing date in 10 business days from current date (Includes bank holidays).
 */
@Service
public class CaseHearingDateSearchService extends ElasticSearchService {

    private static final int BUSINESS_DAYS_FROM_NOW = 10;

    public CaseHearingDateSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        String targetDateString = DateUtils.addDaysSkippingWeekends(
            LocalDate.now(), BUSINESS_DAYS_FROM_NOW).format(DateTimeFormatter.ISO_DATE);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(matchQuery("data.hearingDate", targetDateString))),
            List.of("reference"),
            startIndex
        );
    }
}
