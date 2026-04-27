package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
@Slf4j
public class CoscApplicationSearchService extends ElasticSearchService {

    public CoscApplicationSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to CoscApplicationSearchService query with index {} and timeNow {}", startIndex, timeNow);
        return new Query(
            boolQuery()
                .must(matchQuery("data.coSCApplicationStatus", "Active"))
                .must(rangeQuery("data.coscSchedulerDeadline").lt(timeNow)),
            List.of("reference"),
            startIndex
        );
    }
}
