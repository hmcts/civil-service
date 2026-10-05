package uk.gov.hmcts.reform.civil.service.search.fulladmitpayimmediatelynopayfromdef;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

/**
 * Provides the ElasticSearch query for identifying full admit pay immediately cases where no payment
 * has been received from the defendant. This identifies cases in 'AWAITING_APPLICANT_INTENTION' state
 * where the payment date falls between 8 days ago (skipping weekends) and yesterday, and which have not
 * already been processed by this scheduler.
 */
@Component
@Slf4j
public class FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProvider implements PaginatedQueryProvider {

    private static final int START_INDEX = 0;
    private static final int UPPER_BOUND_DAYS_AGO = 1;
    private static final int LOWER_BOUND_DAYS_AGO = 8;

    private final CommonQueryConstructs commonQueryConstructs;
    private final Time time;

    public FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProvider(CommonQueryConstructs commonQueryConstructs,
                                                                      Time time) {
        this.commonQueryConstructs = commonQueryConstructs;
        this.time = time;
    }

    /**
     * Builds the paginated query for full admit pay immediately cases with no payment from the defendant.
     *
     * @param pageToken the token containing the 'search_after' value
     * @param pageSize the number of results to return
     * @return a PaginatedQuery containing the ES query
     */
    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        LocalDate today = time.now().atZone(LOCAL_ZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDate();
        log.info("Call to FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProvider query with today {}", today);

        return new PaginatedQuery(
            buildNoPaymentReceivedQuery(getLowerBound(today), getUpperBound(today)),
            List.of("reference"),
            START_INDEX,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder buildNoPaymentReceivedQuery(String lowerBound, String upperBound) {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery()
                        .mustNot(matchQuery("data.fullAdmitNoPaymentSchedulerProcessed", "Yes"))
                        .must(rangeQuery("data.respondToClaimAdmitPartLRspec.whenWillThisAmountBePaid")
                                  .gte(lowerBound).lte(upperBound))
                        .must(matchQuery("data.respondent1ClaimResponseTypeForSpec",
                                         RespondentResponseType.FULL_ADMISSION))
                        .must(commonQueryConstructs.beState(AWAITING_APPLICANT_INTENTION))
            );
    }

    private String getUpperBound(LocalDate today) {
        return today.minusDays(UPPER_BOUND_DAYS_AGO).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String getLowerBound(LocalDate today) {
        return DateUtils.addDaysSkippingWeekends(today.minusDays(LOWER_BOUND_DAYS_AGO), 0)
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
