package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class CasesStuckCheckSearchService extends ElasticSearchService {

    public CasesStuckCheckSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
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
        log.info("CasesStuckCheckSearchService: Found {} stuck case(s) in the last 7 days with ids {} at time {}", ids.size(), ids, timeNow);

        return caseDetails;
    }

    public Query query(int startIndex, String timeNow) {
        log.info("Call to CasesStuckCheckSearchService query with index {} and timeNow {}", startIndex, timeNow);
        return new Query(
            boolQuery()
                .should(rangeQuery("data.last_modified").lt(timeNow).gt(
                    "now-7d"))
                .should(boolQuery().mustNot(matchQuery("data.businessProcess.status", "FINISHED"))),
            List.of("reference"),
            startIndex
        );
    }

    @Override
    public Query query(int startIndex) {
        log.info("Call to CasesStuckCheckSearchService query with index {} ", startIndex);
        return query(startIndex, "now");
    }
}
