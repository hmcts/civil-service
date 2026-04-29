package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class BundleCreationTriggerService extends ElasticSearchService {

    private static final int BUNDLE_CREATION_TIME_RANGE = 10;

    public BundleCreationTriggerService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        LocalDate hearingDateCutoff = ZonedDateTime.parse(timeNow)
            .toLocalDate()
            .plusDays(BUNDLE_CREATION_TIME_RANGE);
        log.info(
            "Call to BundleCreationTriggerService query with index {} and hearingDateCutoff {}",
            startIndex,
            hearingDateCutoff
        );
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lte(hearingDateCutoff))
                            .must(beState(CaseState.HEARING_READINESS)))
                .should(boolQuery()
                            .must(rangeQuery("data.hearingDate").lte(hearingDateCutoff))
                            .must(beState(CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING))
                )
                .mustNot(matchQuery("data.allocatedTrack", "MULTI_CLAIM"))
                .mustNot(matchQuery("data.allocatedTrack", "INTERMEDIATE_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "MULTI_CLAIM"))
                .mustNot(matchQuery("data.responseClaimTrack", "INTERMEDIATE_CLAIM")),
            List.of("reference"), startIndex
        );
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
