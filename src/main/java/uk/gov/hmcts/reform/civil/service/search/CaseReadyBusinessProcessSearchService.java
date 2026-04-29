package uk.gov.hmcts.reform.civil.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
public class CaseReadyBusinessProcessSearchService extends ElasticSearchService {

    public CaseReadyBusinessProcessSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        return new Query(
            boolQuery().must(matchQuery("data.businessProcess.status", "READY"))
                .must(rangeQuery("data.businessProcess.readyOn").lt(getReadyOnCutoff(timeNow))),
            List.of(),
            startIndex
        );
    }

    private String getReadyOnCutoff(String timeNow) {
        return ZonedDateTime.parse(timeNow).minusMinutes(5).toString();
    }
}
