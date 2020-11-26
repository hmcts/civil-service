package uk.gov.hmcts.reform.unspec.service.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.model.search.Query;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Service
public class CaseStrikeoutSearchService extends ElasticSearchService {

    public CaseStrikeoutSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    public Query query(int startIndex) {
        return new Query(
            boolQuery()
                .must(rangeQuery("data.applicantSolicitorSecondResponseDeadlineToRespondentSolicitor1").lt("now")),
            List.of("reference"),
            startIndex
        );
    }
}
