package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@Service
public class FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService extends ElasticSearchService {

    private static final int BUSINESS_DAYS_FROM_NOW_MIN = 0;
    private static final int BUSINESS_DAYS_FROM_NOW_MAX = 7;
    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    public FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {

        String toDate = DateUtils.addDaysSkippingWeekends(
            LocalDate.now().minusDays(1), BUSINESS_DAYS_FROM_NOW_MIN).format(DateTimeFormatter.ISO_DATE);

        String fromDate = DateUtils.addDaysSkippingWeekends(
            LocalDate.now().minusDays(1), BUSINESS_DAYS_FROM_NOW_MAX).format(DateTimeFormatter.ISO_DATE);

        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery(
                                "data.respondToClaimAdmitPartLRspec.whenWillThisAmountBePaid")
                                      .lte(toDate).gte(fromDate))
                            .must(matchQuery("data.respondent1ClaimResponseTypeForSpec",
                        RespondentResponseType.FULL_ADMISSION
                    ))
                            .must(beState(AWAITING_APPLICANT_INTENTION))
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
