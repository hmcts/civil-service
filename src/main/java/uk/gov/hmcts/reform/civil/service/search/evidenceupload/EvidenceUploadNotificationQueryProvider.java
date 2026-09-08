package uk.gov.hmcts.reform.civil.service.search.evidenceupload;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.service.search.common.PaginatedQueryProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

/**
 * Provides the ElasticSearch query for identifying cases that need an evidence upload notification.
 * This identifies cases in a hearing related state, which have had a document uploaded in the last
 * 7 days and have not yet had a notification sent.
 */
@Component
@Slf4j
public class EvidenceUploadNotificationQueryProvider implements PaginatedQueryProvider {

    private static final int START_INDEX = 0;
    private static final int NOTIFICATION_WINDOW_DAYS = 7;

    private final CommonQueryConstructs commonQueryConstructs;
    private final Time time;

    public EvidenceUploadNotificationQueryProvider(CommonQueryConstructs commonQueryConstructs, Time time) {
        this.commonQueryConstructs = commonQueryConstructs;
        this.time = time;
    }

    /**
     * Builds the paginated query for cases awaiting an evidence upload notification.
     *
     * @param pageToken the token containing the 'search_after' value
     * @param pageSize the number of results to return
     * @return a PaginatedQuery containing the ES query
     */
    @Override
    public PaginatedQuery getPaginatedQuery(PageToken pageToken, int pageSize) {
        ZonedDateTime now = time.now().atZone(LOCAL_ZONE).withZoneSameInstant(ZoneOffset.UTC);
        log.info("Call to EvidenceUploadNotificationQueryProvider query with timeNow {}", now);

        return new PaginatedQuery(
            buildEvidenceUploadNotificationQuery(now, now.minusDays(NOTIFICATION_WINDOW_DAYS)),
            List.of("reference"),
            START_INDEX,
            pageToken,
            pageSize
        );
    }

    private BoolQueryBuilder buildEvidenceUploadNotificationQuery(ZonedDateTime now, ZonedDateTime windowStart) {
        return boolQuery()
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(commonQueryConstructs.beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                      .should(commonQueryConstructs.beState(HEARING_READINESS))
                      .should(commonQueryConstructs.beState(DECISION_OUTCOME))
                      .should(commonQueryConstructs.beState(All_FINAL_ORDERS_ISSUED))
                      .should(commonQueryConstructs.beState(CASE_PROGRESSION)))
            .mustNot(matchQuery("data.evidenceUploadNotificationSent", "Yes"))
            .must(boolQuery()
                      .minimumShouldMatch(1)
                      .should(rangeQuery("data.caseDocumentUploadDate").lt(now).gt(windowStart))
                      .should(rangeQuery("data.caseDocumentUploadDateRes").lt(now).gt(windowStart)));
    }
}
