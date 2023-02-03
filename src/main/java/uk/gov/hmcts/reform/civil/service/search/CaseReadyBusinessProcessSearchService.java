package uk.gov.hmcts.reform.civil.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class CaseReadyBusinessProcessSearchService extends ElasticSearchService {

    public CaseReadyBusinessProcessSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery().must(matchQuery("data.businessProcess.status", "READY"))
                .must(rangeQuery("data.businessProcess.createdOn").lt(LocalDateTime.now().minusMinutes(5)
                                                               .toString())),
            List.of(),
            startIndex
        );
    }
}
