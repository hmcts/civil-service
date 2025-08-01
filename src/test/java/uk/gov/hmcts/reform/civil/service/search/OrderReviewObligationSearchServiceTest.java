package uk.gov.hmcts.reform.civil.service.search;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.search.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;

class OrderReviewObligationSearchServiceTest extends ElasticSearchServiceTest {

    @BeforeEach
    void setup() {
        searchService = new OrderReviewObligationSearchService(coreCaseDataService);
    }

    @Override
    protected Query buildQuery(int fromValue) {
        BoolQueryBuilder query =  boolQuery()
            .must(boolQuery()
                      .must(existsQuery("data.storedObligationData"))
                      .must(rangeQuery("data.storedObligationData.value.obligationDate")
                                .lte(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)))
                      .must(termQuery("data.storedObligationData.value.obligationWATaskRaised", YesOrNo.NO))
                      .must(boolQuery()
                                .minimumShouldMatch(1)
                                .should(beState(PREPARE_FOR_HEARING_CONDUCT_HEARING))
                                .should(beState(HEARING_READINESS))
                                .should(beState(AWAITING_APPLICANT_INTENTION))
                                .should(beState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT))
                                .should(beState(DECISION_OUTCOME))
                                .should(beState(IN_MEDIATION))
                                .should(beState(All_FINAL_ORDERS_ISSUED))
                                .should(beState(CASE_STAYED))
                                .should(beState(JUDICIAL_REFERRAL))
                                .should(beState(CASE_PROGRESSION))
                      ));

        return new Query(query, List.of("reference"), fromValue);
    }

    private QueryBuilder beState(CaseState caseState) {
        return boolQuery()
            .must(matchQuery("state", caseState.toString()));
    }
}
