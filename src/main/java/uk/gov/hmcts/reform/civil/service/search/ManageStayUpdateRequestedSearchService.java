package uk.gov.hmcts.reform.civil.service.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.ZonedDateTime;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;

@Service
@Slf4j
public class ManageStayUpdateRequestedSearchService extends ElasticSearchService {

    private static final int DAYS = 7;

    public ManageStayUpdateRequestedSearchService(CoreCaseDataService coreCaseDataService) {
        super(coreCaseDataService);
    }

    @Override
    public Query query(int startIndex, String timeNow) {
        log.info("Call to ManageStayUpdateRequestedSearchService query with index {} and timeNow {}", startIndex, timeNow);
        ZonedDateTime cutoff = ZonedDateTime.parse(timeNow).minusDays(DAYS);
        return new Query(
                boolQuery()
                        .minimumShouldMatch(1)
                        .should(boolQuery()
                                    .must(rangeQuery("data.manageStayUpdateRequestDate").lt(cutoff))
                                    .must(matchQuery("state", CASE_STAYED.toString()))
                        ),
                List.of("reference"),
                startIndex
        );
    }
}
