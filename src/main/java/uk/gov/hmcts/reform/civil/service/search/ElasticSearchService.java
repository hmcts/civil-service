package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.math.RoundingMode.UP;

@RequiredArgsConstructor
@Slf4j
public abstract class ElasticSearchService {

    protected static final int START_INDEX = 0;
    protected static final int ES_DEFAULT_SEARCH_LIMIT = 10;
    protected final CoreCaseDataService coreCaseDataService;

    private static final ThreadLocal<String> schedulerStartTime = new ThreadLocal<>();

    public Set<CaseDetails> getCases() {
        try {
            schedulerStartTime.set(ZonedDateTime.now(ZoneOffset.UTC).toString());

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
        } finally {
            // Always clean up ThreadLocal to avoid memory leaks
            schedulerStartTime.remove();
        }
    }

    protected String getSchedulerStartTime() {
        return schedulerStartTime.get();
    }

    abstract Query query(int startIndex);

    protected int calculatePages(SearchResult searchResult) {
        log.info("Initial search result returns total {} case(s)", searchResult.getTotal());
        return new BigDecimal(searchResult.getTotal()).divide(new BigDecimal(ES_DEFAULT_SEARCH_LIMIT), UP).intValue();
    }
}
