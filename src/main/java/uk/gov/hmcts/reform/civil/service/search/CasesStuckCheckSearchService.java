package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.math.RoundingMode.UP;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Service
@Slf4j
@RequiredArgsConstructor
public class CasesStuckCheckSearchService {

    protected static final int START_INDEX = 0;
    protected static final int ES_DEFAULT_SEARCH_LIMIT = 10;
    protected final CoreCaseDataService coreCaseDataService;

    public Set<CaseDetails> getCases() {
        long sweepStarted = System.nanoTime();
        SearchResult searchResult = searchPage(START_INDEX);

        Set<CaseDetails> caseDetailsSet = new HashSet<>(searchResult.getCases());

        int pages = calculatePages(searchResult);
        for (int i = 1; i < pages; i++) {
            SearchResult result = searchPage(i * ES_DEFAULT_SEARCH_LIMIT);
            caseDetailsSet.addAll(result.getCases());
        }

        List<Long> ids = caseDetailsSet.stream().map(CaseDetails::getId).sorted().toList();
        log.info("CasesStuckCheckSearchService: Found {} stuck case(s) across all ages with ids {} at time {}",
                 ids.size(), ids, Instant.now());
        log.info("CasesStuckCheckSearchService: Sweep completed with total={}, returned={}, requests={}, elapsedMs={}",
                 searchResult.getTotal(), ids.size(), Math.max(1, pages),
                 TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - sweepStarted));

        return caseDetailsSet;
    }

    private SearchResult searchPage(int startIndex) {
        long started = System.nanoTime();
        SearchResult result = coreCaseDataService.searchCases(query(startIndex));
        log.info("CasesStuckCheckSearchService: CCD search index={}, total={}, returned={}, elapsedMs={}",
                 startIndex, result.getTotal(), result.getCases().size(),
                 TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        return result;
    }

    public Query query(int startIndex) {
        // A stuck process can outlive any modification window; search all ages and page the results.
        return new Query(
            boolQuery()
                .mustNot(termQuery("data.businessProcess.status.keyword", "finished").caseInsensitive(true)),
            List.of("reference"),
            startIndex
        );
    }

    protected int calculatePages(SearchResult searchResult) {
        log.info("Initially search service found {} case(s) ", searchResult.getTotal());
        return new BigDecimal(searchResult.getTotal()).divide(new BigDecimal(ES_DEFAULT_SEARCH_LIMIT), UP).intValue();
    }
}
