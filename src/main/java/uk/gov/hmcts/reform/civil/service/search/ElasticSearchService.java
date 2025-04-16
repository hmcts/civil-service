package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.math.RoundingMode.UP;

@RequiredArgsConstructor
@Slf4j
public abstract class ElasticSearchService {

    private final CoreCaseDataService coreCaseDataService;
    private static final int START_INDEX = 0;
    private static final int ES_DEFAULT_SEARCH_LIMIT = 10;

    public Set<CaseDetails> getCases() {
        SearchResult searchResult = coreCaseDataService.searchCases(query(START_INDEX));
        int pages = calculatePages(searchResult);
        Set<CaseDetails> caseDetails = new HashSet<>(searchResult.getCases());

        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService.searchCases(query(i * ES_DEFAULT_SEARCH_LIMIT));
            caseDetails.addAll(result.getCases());
        }

        List<Long> ids = caseDetails.stream().map(CaseDetails::getId).sorted().toList();
        log.info("Found {} case(s) with ids {}", ids.size(), ids);

        return caseDetails;
    }

    /**
     * For carm applicable cases, the returned query is sorted by ascending value of case reference. Since the
     * queries are paginated, the value of the last case is used for the search_after property in the query to
     * ensure that cases are returned in the right order of pages to prevent duplication.
     * @param claimMovedDate claimMovedDate
     * @param carmEnabled carmEnabled
     * @return caseDetails
     */
    public List<CaseDetails> getInMediationCases(LocalDate claimMovedDate, boolean carmEnabled) {

        if (claimMovedDate == null) {
            claimMovedDate = LocalDate.now().minusDays(7);
        }
        if (carmEnabled) {
            SearchResult searchResult = coreCaseDataService.searchMediationCases(queryInMediationCases(START_INDEX, claimMovedDate,
                                                                                                       carmEnabled, true, null));
            log.info("mediation total found: {}", searchResult.getTotal());
            int pages = calculatePages(searchResult);
            List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());
            logMediationCaseIds(caseDetails, null);
            // find the last case from that list
            String searchAfterValue = searchResult.getCases().get(searchResult.getCases().size() - 1).getId().toString();
            for (int i = 1; i < pages; i++) {
                // use the query again passing in the search after value
                SearchResult result = coreCaseDataService.searchMediationCases(queryInMediationCases(START_INDEX, claimMovedDate,
                                                                                            carmEnabled, false, searchAfterValue));
                logMediationCaseIds(caseDetails, String.valueOf(i));
                caseDetails.addAll(result.getCases());
                // update the value from the new result
                searchAfterValue = result.getCases().get(result.getCases().size() - 1).getId().toString();
            }
            return caseDetails;
        } else {
            SearchResult searchResult = coreCaseDataService.searchCases(queryInMediationCases(START_INDEX, claimMovedDate,
                                                                                              carmEnabled, false, null));
            int pages = calculatePages(searchResult);
            List<CaseDetails> caseDetails = new ArrayList<>(searchResult.getCases());
            for (int i = 1; i < pages; i++) {
                SearchResult result = coreCaseDataService.searchCases(queryInMediationCases(i * ES_DEFAULT_SEARCH_LIMIT, claimMovedDate,
                                                                                            carmEnabled, false, null));
                caseDetails.addAll(result.getCases());
            }

            return caseDetails;
        }
    }

    abstract Query query(int startIndex);

    abstract Query queryInMediationCases(int startIndex, LocalDate claimMovedDate, boolean carmEnabled, boolean initialSearch,
                                         String searchAfterValue);

    private int calculatePages(SearchResult searchResult) {
        return new BigDecimal(searchResult.getTotal()).divide(new BigDecimal(ES_DEFAULT_SEARCH_LIMIT), UP).intValue();
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
