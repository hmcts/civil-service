package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Service
@Slf4j
public class TrialReadyNotificationSearchService extends ElasticSearchService {

    public TrialReadyNotificationSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to TrialReadyNotificationSearchService query with index {} and timeNow {}", startIndex, timeNow);
        ZonedDateTime cutoff = ZonedDateTime.parse(timeNow)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDate()
            .atTime(LocalTime.MIN)
            .plusWeeks(6)
            .atZone(ZoneOffset.UTC);
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lt(cutoff.toString()))
                            .must(boolQuery()
                                      .minimumShouldMatch(1)
                                      .should(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                                      .should(beState(HEARING_READINESS)))
                            .must(boolQuery()
                                      .minimumShouldMatch(1)
                                      .should(matchQuery("data.allocatedTrack", "FAST_CLAIM"))
                                      .should(matchQuery("data.responseClaimTrack", "FAST_CLAIM")))
                            .mustNot(matchQuery("data.listingOrRelisting", ListingOrRelisting.RELISTING))
                            .mustNot(matchQuery("data.trialReadyNotified", YesOrNo.YES))),
            List.of("reference"),
            startIndex
        );
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
