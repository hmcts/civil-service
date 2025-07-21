package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class DefendantResponseDeadlineCheckSearchService extends ElasticSearchService {

    private final FeatureToggleService featureToggleService;

    public DefendantResponseDeadlineCheckSearchService(CoreCaseDataService coreCaseDataService,
                                                       FeatureToggleService featureToggleService) {
        super(coreCaseDataService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public Query query(int startIndex) {
        log.info("Call to DefendantResponseDeadlineCheckSearchService query with index {} ", startIndex);
        return query(startIndex, "now");
    }

    public Query query(int startIndex, String timeNow) {
        log.info("Call to DefendantResponseDeadlineCheckSearchService query with index {} and timeNow {}", startIndex, timeNow);
        if (featureToggleService.isWelshEnabledForMainCase()) {
            return new Query(
                boolQuery()
                    .minimumShouldMatch(1)
                    .should(boolQuery()
                                .must(rangeQuery("data.respondent1ResponseDeadline").lt(timeNow))
                                .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
                                .mustNot(existsQuery("data.respondent1ResponseDate"))
                                .must(beState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                                .must(haveNoOngoingBusinessProcess())
                    ),
                List.of("reference"),
                startIndex
            );
        }
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(rangeQuery("data.respondent1ResponseDeadline").lt("now"))
                            .mustNot(matchQuery("data.respondent1ResponseDeadlineChecked", "Yes"))
                            .must(beState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                            .must(haveNoOngoingBusinessProcess())
                ),
            List.of("reference"),
            startIndex
        );
    }

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }

    public BoolQueryBuilder haveNoOngoingBusinessProcess() {
        return boolQuery()
            .minimumShouldMatch(1)
            .should(boolQuery().mustNot(existsQuery("data.businessProcess")))
            .should(boolQuery().mustNot(existsQuery("data.businessProcess.status")))
            .should(boolQuery().must(matchQuery("data.businessProcess.status", "FINISHED")));
    }

    @Override
    public Set<CaseDetails> getCases() {
        String timeNow = ZonedDateTime.now(ZoneOffset.UTC).toString();
        SearchResult searchResult = coreCaseDataService.searchCases(query(START_INDEX, timeNow));
        int pages = calculatePages(searchResult);
        Set<CaseDetails> caseDetails = new HashSet<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService.searchCases(query(i * ES_DEFAULT_SEARCH_LIMIT, timeNow));
            caseDetails.addAll(result.getCases());
        }

        List<Long> ids = caseDetails.stream().map(CaseDetails::getId).sorted().toList();
        log.info("Found {} case(s) with ids {}", ids.size(), ids);

        return caseDetails;
    }
}
