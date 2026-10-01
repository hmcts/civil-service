package uk.gov.hmcts.reform.civil.service.search.common;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Component
public class CommonQueryConstructs {

    public BoolQueryBuilder beState(CaseState state) {
        return boolQuery()
            .must(matchQuery("state", state.toString()));
    }
}
