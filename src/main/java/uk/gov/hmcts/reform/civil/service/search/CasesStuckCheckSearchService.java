package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Service
@Slf4j
@RequiredArgsConstructor
public class CasesStuckCheckSearchService {

    protected static final int START_INDEX = 0;
    protected static final int ES_DEFAULT_SEARCH_LIMIT = 10;
    protected final CoreCaseDataService coreCaseDataService;

    public Set<CaseDetails> getCases(String stuckCasesFromPastDays) {
        String timeNow = ZonedDateTime.now(ZoneOffset.UTC).toString();
        SearchResult searchResult = coreCaseDataService.searchCases(query(START_INDEX, timeNow, stuckCasesFromPastDays));

        Set<CaseDetails> caseDetailsSet = new HashSet<>(searchResult.getCases());

        int pages = calculatePages(searchResult);
        for (int i = 1; i < pages; i++) {
            SearchResult result = coreCaseDataService.searchCases(query(i * ES_DEFAULT_SEARCH_LIMIT, timeNow, stuckCasesFromPastDays));
            caseDetailsSet.addAll(result.getCases());
        }

        List<Long> ids = caseDetailsSet.stream().map(CaseDetails::getId).sorted().toList();
        log.info("CasesStuckCheckSearchService: Found {} stuck case(s) in the last 7 days with ids {} at time {}", ids.size(), ids, timeNow);

        return caseDetailsSet;
    }

    public Query query(int startIndex, String timeNow, String stuckCasesFromPastDays) {
        log.info("Call to CasesStuckCheckSearchService query with index {} and timeNow {}", startIndex, timeNow);
        String pastDaysExpression = "now-" + stuckCasesFromPastDays + "d";

        return new Query(
            boolQuery()
                .must(rangeQuery("last_modified").gt(pastDaysExpression).lt(timeNow))
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
