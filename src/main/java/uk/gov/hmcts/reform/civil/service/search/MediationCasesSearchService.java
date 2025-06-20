package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;

@Service
@Slf4j
public class MediationCasesSearchService extends ElasticSearchService {

    private static final LocalDateTime CARM_DATE = LocalDateTime.of(2024, 11, 5,
                                                                    7, 28, 35
    );

    public MediationCasesSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }

    private QueryBuilder submittedDate(boolean carmEnabled) {
        if (carmEnabled) {
            return boolQuery()
                .must(rangeQuery("data.submittedDate").gte(CARM_DATE));
        } else {
            return boolQuery()
                .must(rangeQuery("data.submittedDate").lt(CARM_DATE));
        }
    }

    /**
     * For carm applicable cases, the returned query is sorted by ascending value of case reference. Since the
     * queries are paginated, the value of the last case is used for the search_after property in the query to
     * ensure that cases are returned in the right order of pages to prevent duplication.
     *
     * @param carmEnabled carmEnabled
     * @return caseDetails
     */
    public List<CaseDetails> getInMediationCases(boolean carmEnabled) {
        if (carmEnabled) {
            SearchResult searchResult =
                coreCaseDataService.searchMediationCases(queryInMediationCases(START_INDEX,
                                                                               carmEnabled, true,
                                                                               null
                ));
            log.info("mediation total found: {}", searchResult.getTotal());
            int pages = calculatePages(searchResult);
            List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());
            if (caseDetails != null && !caseDetails.isEmpty()) {
                logMediationCaseIds(caseDetails, null);
                // find the last case from that list
                String searchAfterValue = searchResult.getCases().get(searchResult.getCases().size() - 1).getId().toString();
                for (int i = 1; i < pages; i++) {
                    // use the query again passing in the search after value
                    SearchResult result = coreCaseDataService.searchMediationCases(queryInMediationCases(
                        START_INDEX,
                        carmEnabled,
                        false,
                        searchAfterValue
                    ));
                    logMediationCaseIds(caseDetails, String.valueOf(i));
                    caseDetails.addAll(result.getCases());
                    // update the value from the new result
                    searchAfterValue = result.getCases().get(result.getCases().size() - 1).getId().toString();
                }
            }
            return caseDetails;
        } else {
            SearchResult searchResult = coreCaseDataService.searchCases(queryInMediationCases(
                START_INDEX,
                carmEnabled,
                false,
                null
            ));
            int pages = calculatePages(searchResult);
            List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());
            for (int i = 1; i < pages; i++) {
                SearchResult result = coreCaseDataService.searchCases(queryInMediationCases(
                    i * ES_DEFAULT_SEARCH_LIMIT,
                    carmEnabled,
                    false,
                    null
                ));
                caseDetails.addAll(result.getCases());
            }

            return caseDetails;
        }
    }

    @Override
    Query query(int startIndex) {
        return null;
    }

    private Query queryInMediationCases(int startIndex, boolean carmEnabled, boolean initialSearch,
                                String searchAfterValue) {

        if (carmEnabled) {
            return new Query(
                boolQuery()
                    .must(matchAllQuery())
                    .must(beState(IN_MEDIATION))
                    .must(submittedDate(carmEnabled))
                    .must(rangeQuery("data.claimMovedToMediationOn")
                              .gt("now-8d/d").lt("now/d"))
                    .mustNot(matchQuery("data.mediationFileSentToMmt", "Yes")),
                emptyList(),
                startIndex,
                initialSearch,
                searchAfterValue
            );
        }
        return new Query(
            boolQuery()
                .minimumShouldMatch(1)
                .should(boolQuery()
                            .must(beState(IN_MEDIATION))
                            .must(submittedDate(carmEnabled))
                            .must(rangeQuery("data.claimMovedToMediationOn")
                                      .gt("now-8d/d").lt("now/d")))
                .mustNot(matchQuery("data.mediationFileSentToMmt", "Yes")),
            emptyList(),
            startIndex
        );
    }

    private void logMediationCaseIds(List<CaseDetails> caseDetails, String page) {
        if (!caseDetails.isEmpty()) {
            StringBuilder sb = new StringBuilder().append("Mediation query case IDs: ");
            if (page != null) {
                sb.append("page ").append(page).append(" ");
            }
            for (CaseDetails caseDetail : caseDetails) {
                sb.append(caseDetail.getId());
                sb.append("\n");
            }
            log.info(sb.toString());
        }
    }
}
