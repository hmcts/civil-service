package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Service
public class TrialReadyNotificationSearchService extends ElasticSearchService {

    public TrialReadyNotificationSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lt(LocalDate.now()
                                                                        .atTime(LocalTime.MIN)
                                                                        .plusWeeks(6).toString()))
                            .must(boolQuery()
                                      .minimumShouldMatch(1)
                                      .should(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                                      .should(beState(HEARING_READINESS)))
                            .mustNot(matchQuery("data.allocatedTrack", "SMALL_CLAIM"))
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
