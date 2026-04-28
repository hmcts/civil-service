package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class JudgmentRequestedSearchService extends ElasticSearchService {

    public JudgmentRequestedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        LocalDate fortyEightHoursAgo = LocalDate.now().minusDays(2);
        log.info("Call to JudgmentRequestedSearchService query with index {} and requestDate <= {}",
                 startIndex, fortyEightHoursAgo);
        return new Query(
            boolQuery()
                .must(rangeQuery("data.activeJudgment.requestDate").lte(fortyEightHoursAgo)),
            List.of("reference"),
            startIndex
        );
    }
}
