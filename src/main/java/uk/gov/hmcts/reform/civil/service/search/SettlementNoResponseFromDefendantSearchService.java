package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
public class SettlementNoResponseFromDefendantSearchService extends ElasticSearchService {

    private static final int BUSINESS_DAYS_FROM_NOW = 0;
    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    public SettlementNoResponseFromDefendantSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {

        String targetDateString = DateUtils.addDaysSkippingWeekends(
                LocalDate.now().minusDays(1), BUSINESS_DAYS_FROM_NOW).atTime(END_OF_BUSINESS_DAY)
            .format(DateTimeFormatter.ISO_DATE);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(matchQuery(
                                "data.respondent1RespondToSettlementAgreementDeadline",
                                targetDateString
                            ))
                            .must(beState(CaseState.AWAITING_APPLICANT_INTENTION))
                ),
            List.of("reference"),
            startIndex
        );
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
