package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

class FullAdmitPayImmediatelyNoPaymentFromDefendantSearchServiceTest extends ElasticSearchServiceTest {

    private static final int BUSINESS_DAYS_FROM_NOW = 0;

    @BeforeEach
    void setup() {
        searchService = new FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDate upperBound = now.toLocalDate().minusDays(1);
        LocalDate lowerBound = DateUtils.addDaysSkippingWeekends(now.toLocalDate().minusDays(8), BUSINESS_DAYS_FROM_NOW);
        String expectedLower = lowerBound.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String expectedUpper = upperBound.format(DateTimeFormatter.ISO_LOCAL_DATE);
        BoolQueryBuilder query = boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .mustNot(matchQuery("data.fullAdmitNoPaymentSchedulerProcessed", "Yes"))
                        .must(rangeQuery(
                            "data.respondToClaimAdmitPartLRspec.whenWillThisAmountBePaid")
                                  .gte(expectedLower).lte(expectedUpper))
                        .must(matchQuery(
                            "data.respondent1ClaimResponseTypeForSpec",
                            RespondentResponseType.FULL_ADMISSION
                        ))
                        .must(boolQuery().must(matchQuery("state", "AWAITING_APPLICANT_INTENTION"))));
        return new Query(query, List.of("reference"), fromValue);
    }
}
